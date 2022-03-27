package org.hyt.hytport.visual.service

import android.content.*
import android.media.AudioManager
import android.os.Bundle
import android.os.IBinder
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import org.hyt.hytport.R
import org.hyt.hytport.audio.api.service.HYTBinder
import org.hyt.hytport.audio.factory.HYTAudioFactory
import org.hyt.hytport.audio.service.HYTService

abstract class HYTBaseActivity: AppCompatActivity() {

    protected var _auditor: HYTBinder.Companion.HYTAuditor? = null;

    protected lateinit var _player: HYTBinder;

    private lateinit var _connection: ServiceConnection;

    protected var _bound: Boolean = false;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferences: SharedPreferences = getSharedPreferences(
            resources.getString(R.string.preferences),
            Context.MODE_PRIVATE
        );
        val preferenceName = resources.getString(R.string.preferences_permissions);
        if (!preferences.getBoolean(preferenceName, false)) {
            startActivityIfNeeded(Intent(this, HYTInit::class.java), 100);
            finish();
            return;
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        window.navigationBarColor = getColor(R.color.hyt_transparent);
        supportActionBar?.hide();
        volumeControlStream = AudioManager.STREAM_MUSIC;
        _connection = object : ServiceConnection {

            override fun onServiceConnected(component: ComponentName?, binder: IBinder?) {
                _player = binder as HYTBinder;
                _auditor = _getAuditor();
                if (_auditor != null){
                    _player.addAuditor(_auditor!!);
                }
                _preparePlayer();
                if (_player.getRepository() == null) {
                    _player.setRepository(HYTAudioFactory.getAudioRepository(contentResolver));
                }
                _bound = true;
            }

            override fun onServiceDisconnected(component: ComponentName?) {
                _bound = false;
            }

        };
        _startPlayer();
    }

    protected fun _startPlayer(): Unit{
        if (!_bound){
            startService(Intent(this, HYTService::class.java));
            bindService(Intent(this, HYTService::class.java), _connection, 0);
        }
    }

    protected abstract fun _getAuditor(): HYTBinder.Companion.HYTAuditor?;

    protected open fun _preparePlayer(): Unit {}

    override fun onDestroy() {
        if (_auditor != null && _bound){
            _player.removeAuditor(_auditor!!);
        }
        unbindService(_connection);
        super.onDestroy();
    }

}