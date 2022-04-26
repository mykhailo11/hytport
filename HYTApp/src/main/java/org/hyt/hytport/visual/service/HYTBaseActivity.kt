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
import org.hyt.hytport.audio.api.access.HYTAudioRepository
import org.hyt.hytport.audio.api.model.HYTAudioModel
import org.hyt.hytport.audio.api.service.HYTBinder
import org.hyt.hytport.audio.factory.HYTAudioFactory
import org.hyt.hytport.audio.service.HYTService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

abstract class HYTBaseActivity : ComponentActivity() {

    private var _connection: ServiceConnection? = null;

    private var _executor: ScheduledExecutorService? = null;

    protected lateinit var _preferences: SharedPreferences;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        _preferences = getSharedPreferences(
            resources.getString(R.string.preferences),
            Context.MODE_PRIVATE
        );
        val preferenceName = resources.getString(R.string.preferences_permissions);
        if (!_preferences.getBoolean(preferenceName, false)) {
            startActivityIfNeeded(Intent(this, HYTInit::class.java), 100);
            finish();
            return;
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        window.navigationBarColor = getColor(R.color.hyt_transparent);
        volumeControlStream = AudioManager.STREAM_MUSIC;
        _executor = Executors.newSingleThreadScheduledExecutor();
        val intent: Intent = Intent(this, HYTService::class.java);
        setContent {
            var player: HYTBinder? by remember { mutableStateOf(null) };
            val connection: ServiceConnection by remember(player) {
                derivedStateOf {
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
                compose(player!!, _executor!!);
            } else {
                loading();
            }
        }
    }

    @Composable
    protected abstract fun compose(player: HYTBinder, executor: ScheduledExecutorService);

    @Composable
    protected open fun loading(){}

    override fun onDestroy() {
        _executor?.shutdown();
        if (_connection != null) {
            unbindService(_connection!!);
        }
        super.onDestroy()
    }

}