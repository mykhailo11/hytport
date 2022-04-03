package org.hyt.hytport.visual.service

import android.content.*
import android.media.AudioManager
import android.os.Bundle
import android.os.IBinder
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import org.hyt.hytport.R
import org.hyt.hytport.audio.api.service.HYTBinder
import org.hyt.hytport.audio.service.HYTService
import org.hyt.hytport.visual.service.HYTInit

abstract class HYTBaseActivity : ComponentActivity() {

    private var _connection: ServiceConnection? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
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
        volumeControlStream = AudioManager.STREAM_MUSIC;
        val intent: Intent = Intent(this, HYTService::class.java);
        setContent {
            var player: HYTBinder? by remember { mutableStateOf(null) };
            val connection: ServiceConnection by derivedStateOf {
                object : ServiceConnection {

                    override fun onServiceConnected(component: ComponentName?, binder: IBinder?) {
                        if (binder != null) {
                            player = binder as HYTBinder?;
                        }
                        _connection = this;
                    }

                    override fun onServiceDisconnected(component: ComponentName?) {
                        unbindService(this);
                        player = null;
                        _connection = null;
                    }

                }
            }
            val bound: Boolean by produceState(
                initialValue = false,
                player
            ) {
                if (player == null) {
                    value = false;
                    startService(intent);
                    bindService(intent, connection, 0);
                } else {
                    value = true;
                }
            }
            if (bound && player != null) {
                compose(player!!);
            }else {
                loading();
            }
        }
    }

    @Composable
    protected abstract fun compose(player: HYTBinder);

    @Composable
    protected open fun loading(){}

    override fun onDestroy() {
        if (_connection != null) {
            unbindService(_connection!!);
        }
        super.onDestroy()
    }

}