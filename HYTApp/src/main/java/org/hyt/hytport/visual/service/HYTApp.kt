package org.hyt.hytport.visual.service

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.WindowManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import org.hyt.hytport.R
import org.hyt.hytport.audio.api.model.HYTAudioModel
import org.hyt.hytport.audio.api.service.HYTBinder
import org.hyt.hytport.graphics.factory.HYTGLFactory
import org.hyt.hytport.util.HYTMathUtil
import org.hyt.hytport.util.HYTUtil
import org.hyt.hytport.visual.api.model.HYTState
import org.hyt.hytport.visual.component.loading.loadingIcon
import org.hyt.hytport.visual.component.player.cover
import org.hyt.hytport.visual.component.player.player
import org.hyt.hytport.visual.component.surface.pop
import org.hyt.hytport.visual.component.surface.surface
import org.hyt.hytport.visual.factory.HYTStateFactory
import java.util.concurrent.ScheduledExecutorService

class HYTApp : HYTBaseActivity() {

    companion object {

        private val _STATES: Int = 6;

    }

    private lateinit var _canvas: GLSurfaceView.Renderer;

    private lateinit var _consumer: (ByteArray) -> Unit;

    private var _paused: ((Boolean) -> Unit)? = null;

    private lateinit var _parameters: Array<HYTState>;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        val pulseStates: Array<HYTState> = Array(_STATES) {
            HYTStateFactory.getPulseState(0.02f);
        };
        val balanceStates: Array<HYTState> = Array(_STATES) {
            HYTStateFactory.getBalanceState(0.02f);
        }
        _parameters = arrayOf(
            HYTStateFactory.getBalanceState(0.04f)
        );
        _consumer = { food: ByteArray ->
            val result: Float = HYTMathUtil.getNormalBytesAverage(food);
            val chosen: Int = (Math.random() * _STATES).toInt();
            balanceStates[chosen].setState(result);
            if (result > 0.01) {
                pulseStates[chosen].setState(result);
            }
        }
        _canvas = HYTGLFactory.getCanvas(
            this,
            HYTUtil.readSource(resources.getString(R.string.vertex_shader), assets),
            HYTUtil.readSource(resources.getString(R.string.fragment_shader), assets),
            mapOf(
                Pair(resources.getString(R.string.pulse_states), pulseStates),
                Pair(resources.getString(R.string.balance_states), balanceStates),
                Pair(resources.getString(R.string.parameters), _parameters)
            )
        );
    }

    @Composable
    override fun compose(player: HYTBinder, executor: ScheduledExecutorService) {
        val context: Context = LocalContext.current;
        val paused: Boolean by produceState(
            initialValue = false,
            context
        ) {
            _paused = { pause: Boolean ->
                value = pause;
            }
        };
        var visible: Boolean by remember { mutableStateOf(true) };
        val opacity: Float by animateFloatAsState(if (visible) 1.0f else 0.0f);
        var cover: Bitmap? by remember { mutableStateOf(null) };
        var instructor: Boolean by remember {
            mutableStateOf(
                _preferences.getBoolean(resources.getString(R.string.preferences_instructor), true)
            )
        };
        val auditor: HYTBinder.Companion.HYTAuditor by remember(player, context) {
            derivedStateOf {
                object : HYTBinder.Companion.HYTAuditor {

                    override fun consumer(food: ByteArray) {
                        _consumer(food);
                    }

                    override fun onReady(audio: HYTAudioModel) {
                        cover = HYTUtil.getBitmap(audio.getAlbumPath(), context.contentResolver);
                    }

                    override fun onNext(audio: HYTAudioModel) {
                        cover = HYTUtil.getBitmap(audio.getAlbumPath(), context.contentResolver);
                    }

                    override fun onPrevious(audio: HYTAudioModel) {
                        cover = HYTUtil.getBitmap(audio.getAlbumPath(), context.contentResolver);
                    }

                }
            }
        };
        DisposableEffect(player) {
            player.addAuditor(auditor);
            onDispose {
                player.removeAuditor(auditor);
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            surface(
                renderer = _canvas,
                click = {
                    visible = !visible;
                    if (visible) {
                        _parameters[0].setState(0.0f)
                    } else {
                        _parameters[0].setState(1.0f)
                    }
                },
                longClick = {
                    startActivityIfNeeded(Intent(context, HYTLibrary::class.java), 100);
                },
                paused = paused
            );
            val animating: Boolean = visible || opacity > 0.0f
            val configuration: Configuration = LocalConfiguration.current;
            if (
                animating
                && configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
                && configuration.screenWidthDp > 500
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .alpha(opacity)
                        .background(
                            color = colorResource(R.color.hyt_player_back)
                        )
                        .fillMaxSize()
                        .padding(50.dp)
                ) {
                    cover(
                        album = cover
                    )
                    player(
                        player = player,
                        longClick = {
                            startActivityIfNeeded(Intent(context, HYTLibrary::class.java), 100);
                        },
                        modifier = Modifier
                            .weight(
                                weight = 1.0f,
                                fill = true
                            )
                            .padding(
                                horizontal = 50.dp,
                                vertical = 0.dp
                            )
                    );
                }
            } else if (
                animating
                && configuration.orientation == Configuration.ORIENTATION_PORTRAIT
                && configuration.screenHeightDp > 500
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .alpha(opacity)
                        .background(
                            color = colorResource(R.color.hyt_player_back)
                        )
                        .fillMaxSize()
                        .padding(50.dp)
                ) {
                    cover(
                        album = cover
                    )
                    player(
                        player = player,
                        longClick = {
                            startActivityIfNeeded(Intent(context, HYTLibrary::class.java), 100);
                        },
                        modifier = Modifier
                            .weight(
                                weight = 1.0f,
                                fill = true
                            )
                            .padding(20.dp)
                    );
                }
            } else if (animating) {
                player(
                    player = player,
                    longClick = {
                        startActivityIfNeeded(Intent(context, HYTLibrary::class.java), 100);
                    },
                    modifier = Modifier
                        .alpha(opacity)
                        .background(
                            color = colorResource(R.color.hyt_player_back)
                        )
                        .fillMaxSize()
                        .padding(50.dp)
                );
            }
            val instructorAnimation: Float by animateFloatAsState(if (instructor) 1.0f else 0.0f);
            if (instructor || instructorAnimation > 0.0f) {
                pop(
                    title = remember { "WELCOME" },
                    content = buildAnnotatedString {
                        append("- tap on screen to view controls\n")
                        append("- hold screen or play button to open library")
                    },
                    contentClick = null,
                    confirm = remember { "OK" },
                    accept = {
                        _preferences
                            .edit()
                            .putBoolean(resources.getString(R.string.preferences_instructor), false)
                            .apply();
                        instructor = false;
                    },
                    modifier = Modifier
                        .background(
                            color = colorResource(R.color.hyt_instructor_back)
                        )
                        .alpha(instructorAnimation)
                );
            }
        }
    }

    override fun onPause() {
        if (_paused != null) {
            _paused!!(true);
        }
        super.onPause()
    }

    override fun onResume() {
        if (_paused != null) {
            _paused!!(false);
        }
        super.onResume()
    }

    @Composable
    override fun loading() {
        loadingIcon();
    }

    override fun onDestroy() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super.onDestroy();
    }
}