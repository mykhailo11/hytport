package org.hyt.hytport.visual.model

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.widget.Button
import org.hyt.hytport.R

class HYTInit : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (_check()){
            startActivity(Intent(this, HYTActivity::class.java));
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        supportActionBar?.hide();
        setContentView(R.layout.hyt_init);
        val button: Button = findViewById(R.id.hyt_confirm);
        button.setOnClickListener{
            requestPermissions(
                arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.MODIFY_AUDIO_SETTINGS,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.FOREGROUND_SERVICE,
                    Manifest.permission.INTERNET
                ),
                300
            );
        }
    }

    private fun _check(): Boolean{
        return checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.MODIFY_AUDIO_SETTINGS) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED;
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (_check()){
            startActivity(Intent(this, HYTActivity::class.java));
        }
    }

}