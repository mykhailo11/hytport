package org.hyt.hytport.visual.service

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Window
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import org.hyt.hytport.R

class HYTInit : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (_check()) {
            startActivity(Intent(this, HYTApp::class.java));
            finish();
            return;
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        supportActionBar?.hide();
        setContentView(R.layout.hyt_init);
        val button: Button = findViewById(R.id.hyt_confirm);
        button.setOnClickListener {
            requestPermissions(
                arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.MODIFY_AUDIO_SETTINGS,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.FOREGROUND_SERVICE,
                    Manifest.permission.INTERNET,
                    Manifest.permission.WAKE_LOCK
                ),
                300
            );
        }
    }

    private fun _check(): Boolean {
        return checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.MODIFY_AUDIO_SETTINGS) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && (
                    Build.VERSION.SDK_INT < Build.VERSION_CODES.P ||
                    checkSelfPermission(Manifest.permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_GRANTED
                )
                && checkSelfPermission(Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.WAKE_LOCK) == PackageManager.PERMISSION_GRANTED;
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (_check()) {
            getSharedPreferences(
                resources.getString(R.string.preferences),
                Context.MODE_PRIVATE
            )
                .edit()
                .putBoolean(
                    resources.getString(R.string.preferences_permissions),
                    true
                )
                .commit();
            startActivityIfNeeded(Intent(this, HYTApp::class.java), 100);
            finish();
        }
    }

}