package org.hyt.hytport.visual.model

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.media.AudioManager
import android.os.Bundle
import android.os.IBinder
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import org.hyt.hytport.audio.api.model.HYTAudioPlayer
import org.hyt.hytport.audio.api.service.HYTBinder
import org.hyt.hytport.audio.model.HYTService

abstract class HYTBaseActivity: AppCompatActivity() {

    protected var _audit: Int = -1;

    protected lateinit var _player: HYTBinder;

    private lateinit var _connection: ServiceConnection;

    protected var _bound: Boolean = false;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        supportActionBar?.hide();
        volumeControlStream = AudioManager.STREAM_MUSIC;
        _connection = object : ServiceConnection{

            override fun onServiceConnected(component: ComponentName?, binder: IBinder?) {
                _player = binder as HYTBinder;
                _audit = _player.addAudit(_getAudit());
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

    protected abstract fun _getAudit(): HYTAudioPlayer.HYTAudioPlayerAudit;

    override fun onDestroy() {
        if (_audit != -1 && _bound){
            _player.removeAudit(_audit);
        }
        super.onDestroy();
    }

}