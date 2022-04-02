package org.hyt.hytport.visual.service

import android.content.*
import android.media.AudioManager
import android.os.Bundle
import android.os.IBinder
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import org.hyt.hytport.R
import org.hyt.hytport.audio.api.service.HYTBinder
import org.hyt.hytport.visual.old.service.HYTInit
import androidx.compose.runtime.*;
import androidx.compose.ui.platform.LocalContext
import org.hyt.hytport.audio.service.HYTService

abstract class HYTBaseActivity : ComponentActivity() {

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
        setContent {
            var player: HYTBinder? by remember { mutableStateOf(null) };
            val context: Context = LocalContext.current;
            val intent: Intent by derivedStateOf {
                Intent(context, HYTService::class.java);
            };
            val connection: ServiceConnection by derivedStateOf {
                object : ServiceConnection {

                    override fun onServiceConnected(component: ComponentName?, binder: IBinder?) {
                        if (binder != null) {
                            player = binder as HYTBinder?;
                        }
                    }

                    override fun onServiceDisconnected(component: ComponentName?) {
                        context.unbindService(this);
                        player = null;
                    }

                }
            }
            val bound: Boolean by produceState(
                initialValue = false,
                player
            ) {
                if (player == null) {
                    value = false;
                    context.startService(intent);
                    context.bindService(intent, connection, 0);
                } else {
                    value = true;
                }
            }
            DisposableEffect(context) {
                onDispose {
                    if (bound) {
                        context.unbindService(connection);
                        player = null;
                    }
                }
            }
            if (player != null) {
                compose(player!!);
            }
        }
    }

    @Composable
    protected abstract fun compose(player: HYTBinder);

    override fun onDestroy() {
        super.onDestroy()
    }

}