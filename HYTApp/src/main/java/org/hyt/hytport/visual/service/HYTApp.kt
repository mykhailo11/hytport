package org.hyt.hytport.visual.service

import android.app.Activity
import android.content.*
import android.content.res.Configuration
import android.graphics.Bitmap
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.os.IBinder
import android.view.WindowManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.hyt.hytport.R
import org.hyt.hytport.audio.api.model.HYTAudioManager
import org.hyt.hytport.audio.api.model.HYTAudioModel
import org.hyt.hytport.audio.api.service.HYTBinder
import org.hyt.hytport.graphics.factory.HYTGLFactory
import org.hyt.hytport.neural.service.HYTNeuralService
import org.hyt.hytport.util.HYTMathUtil
import org.hyt.hytport.util.HYTUtil
import org.hyt.hytport.visual.api.model.HYTPlayerState
import org.hyt.hytport.visual.api.model.HYTPopState
import org.hyt.hytport.visual.api.model.HYTState
import org.hyt.hytport.visual.component.loading.loadingIcon
import org.hyt.hytport.visual.component.player.cover
import org.hyt.hytport.visual.component.player.player
import org.hyt.hytport.visual.component.player.rememberPlayerState
import org.hyt.hytport.visual.component.surface.pop
import org.hyt.hytport.visual.component.surface.rememberPopState
import org.hyt.hytport.visual.component.surface.surface
import org.hyt.hytport.visual.component.util.pressed
import org.hyt.hytport.visual.factory.HYTStateFactory
import java.util.concurrent.ScheduledExecutorService

class HYTApp : HYTBaseActivity() {

    companion object {

        private val _STATES: Int = 5;

    }

    private lateinit var _canvas: GLSurfaceView.Renderer;

    private lateinit var _consumer: (ByteArray) -> Unit;

    private var _paused: ((Boolean) -> Unit)? = null;

    private lateinit var _parameters: Array<HYTState>;

    private var _preferencesAuditor: SharedPreferences.OnSharedPreferenceChangeListener? = null;

    private var _neuralConnection: ServiceConnection? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        var neuralBinder: HYTNeuralService.Companion.HYTBinder? = null;
        _neuralConnection = object : ServiceConnection {

            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                neuralBinder = binder as HYTNeuralService.Companion.HYTBinder?;
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                neuralBinder = null;
            }

        }
        bindService(
            Intent(this, HYTNeuralService::class.java),
            _neuralConnection!!,
            Context.BIND_AUTO_CREATE
        );
        val speed: Float = 60.0f / _rate;
        val pulseStates: Array<HYTState> = Array(_STATES) {
            HYTStateFactory.getPulseState(0.015f * speed);
        };
        val balanceStates: Array<HYTState> = Array(_STATES) {
            HYTStateFactory.getBalanceState(0.03f * speed);
        }
        _parameters = arrayOf(
            HYTStateFactory.getBalanceState(0.04f),
            HYTStateFactory.getBalanceState(0.01f * speed),
            HYTStateFactory.getBalanceState(0.005f)
        );
        _consumer = { food: ByteArray ->
            val array: Array<Float> = HYTMathUtil.getNormalByteArray(food);
            if (Math.random() > 0.9f) {
                val prediction: Array<Float>? = neuralBinder?.predict(array);
                val color: Float = prediction?.get(0) ?: 0.5f;
                _parameters[2].setState(color);
            }
            val result: Float = HYTMathUtil.getNormalBytesAverage(array);
            val chosen: Int = (Math.random() * _STATES).toInt();
            balanceStates[chosen].setState(result);
            if (result > 0.02) {
                pulseStates[chosen].setState(result);
            }
            _parameters[1].setState(
                pulseStates.maxOf { state: HYTState ->
                    state.getState();
                }
            )
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
        val activity: Activity = this;
        val playerState: HYTPlayerState = rememberPlayerState();
        DisposableEffect(playerState) {
            val wakeLock: String = resources.getString(R.string.settings_wake_lock);
            val expandLock: String = resources.getString(R.string.setting_show_expanded);
            _preferencesAuditor = SharedPreferences.OnSharedPreferenceChangeListener { preferences: SharedPreferences,
                                                                                       key: String ->
                when {
                    key == wakeLock && preferences.getBoolean(wakeLock, true)
                        -> window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    key == wakeLock -> window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    key == expandLock -> playerState.lock(preferences.getBoolean(expandLock, false));
                }
            }
            _preferences.registerOnSharedPreferenceChangeListener(_preferencesAuditor);
            _preferencesAuditor?.onSharedPreferenceChanged(_preferences, wakeLock);
            _preferencesAuditor?.onSharedPreferenceChanged(_preferences, expandLock);
            onDispose {
                if (_preferencesAuditor != null) {
                    _preferences.unregisterOnSharedPreferenceChangeListener(_preferencesAuditor);
                }
            }
        }
        val paused: Boolean by produceState(
            initialValue = false,
            context
        ) {
            _paused = { pause: Boolean ->
                value = pause;
            }
        };
        var visible: Boolean by rememberSaveable { mutableStateOf(true) };
        LaunchedEffect(visible) {
            if (visible) {
                _parameters[0].setState(0.0f)
            } else {
                _parameters[0].setState(1.0f)
            }
        }
        var instructor: Boolean by remember {
            mutableStateOf(
                _preferences.getBoolean(resources.getString(R.string.preferences_instructor), true)
            )
        };
        val instructorAnimation: Float by animateFloatAsState(if (instructor) 1.0f else 0.0f);
        val opacity: Float by animateFloatAsState(if (visible || instructor) 1.0f - instructorAnimation else 0.0f);
        var cover: Bitmap? by remember { mutableStateOf(null) };
        val auditor: HYTBinder.Companion.HYTAuditor by remember(player, context) {
            derivedStateOf {
                object : HYTBinder.Companion.HYTAuditor {

                    override fun consumer(food: ByteArray) {
                        _consumer(food);
                    }

                    override fun onReady(audio: HYTAudioModel?, current: Long) {
                        if (audio != null) {
                            cover = HYTUtil.getBitmap(audio.getAlbumPath(), context.contentResolver);
                        }
                    }

                    override fun onNext(audio: HYTAudioModel) {
                        cover = HYTUtil.getBitmap(audio.getAlbumPath(), context.contentResolver);
                    }

                    override fun onPrevious(audio: HYTAudioModel) {
                        cover = HYTUtil.getBitmap(audio.getAlbumPath(), context.contentResolver);
                    }

                    override fun onSetManager(manager: HYTAudioManager, audio: HYTAudioModel?) {
                        cover = HYTUtil.getBitmap(audio?.getAlbumPath(), context.contentResolver);
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
        BoxWithConstraints(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
        ) {
            var pressed: Boolean by remember { mutableStateOf(false) };
            val backColor: Color by animateColorAsState(
                if (!pressed) colorResource(R.color.hyt_player_back)
                else colorResource(R.color.hyt_player_back_press)
            );
            surface(
                renderer = _canvas,
                paused = paused,
                modifier = Modifier
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = pressed(
                                pressed = {
                                    pressed = true;
                                },
                                react = {
                                    pressed = false;
                                }
                            ),
                            onLongPress = {
                                startActivityIfNeeded(Intent(context, HYTLibrary::class.java), 100);
                            },
                            onTap = {
                                visible = !visible || instructor;
                            }
                        )
                    }
            );
            val animating: Boolean = (visible || opacity > 0.0f) && (instructorAnimation < 1.0f)
            val configuration: Configuration = LocalConfiguration.current;
            if (
                animating
                && configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
                && maxWidth > 700.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .alpha(opacity - instructorAnimation)
                        .background(
                            color = backColor
                        )
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(30.dp)
                ) {
                    cover(
                        album = cover
                    )
                    player(
                        player = player,
                        playerState = playerState,
                        activity = activity,
                        modifier = Modifier
                            .weight(
                                weight = 1.0f,
                                fill = false
                            )
                            .padding(
                                start = 30.dp
                            )
                    );
                }
            } else if (
                animating
                && configuration.orientation == Configuration.ORIENTATION_PORTRAIT
                && maxHeight > 600.dp
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .alpha(opacity - instructorAnimation)
                        .background(
                            color = backColor
                        )
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(30.dp)
                ) {
                    cover(
                        album = cover
                    )
                    player(
                        player = player,
                        playerState = playerState,
                        activity = activity,
                        modifier = Modifier
                            .weight(1.0f)
                            .padding(10.dp)
                    );
                }
            } else if (animating) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = backColor
                        )
                ) {
                    player(
                        player = player,
                        playerState = playerState,
                        activity = activity,
                        modifier = Modifier
                            .alpha(opacity - instructorAnimation)
                            .fillMaxSize()
                            .padding(30.dp)
                    );
                }
            }
            if (instructor || instructorAnimation > 0.0f) {
                val popState: HYTPopState = rememberPopState(
                    initialTitle = remember { "Welcome" },
                    initialConfirm = remember { "OK" },
                    initialContent = buildAnnotatedString {
                        pushStyle(
                            SpanStyle(
                                color = colorResource(R.color.hyt_white),
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                        append("- tap on screen to view controls\n")
                        append("- hold screen to open library")
                        append("- hold play button to expand controls")
                        pop();
                    }
                );
                DisposableEffect(popState) {
                    val auditor: HYTPopState.Companion.HYTAuditor = object:
                        HYTPopState.Companion.HYTAuditor {

                        override fun onAccept() {
                            _preferences
                                .edit()
                                .putBoolean(resources.getString(R.string.preferences_instructor), false)
                                .apply();
                            instructor = false;
                        }

                    }
                    popState.addAuditor(auditor);
                    onDispose {
                        popState.removeAuditor(auditor);
                    }
                }
                pop(
                    state = popState,
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
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (_neuralConnection != null) {
            unbindService(_neuralConnection!!);
        }
        super.onDestroy();
    }
}