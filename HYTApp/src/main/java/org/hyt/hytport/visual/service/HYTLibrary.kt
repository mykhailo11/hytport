package org.hyt.hytport.visual.service

import android.content.Context
import android.opengl.GLSurfaceView
import android.os.Bundle
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import org.hyt.hytport.R
import org.hyt.hytport.audio.api.model.HYTAudioModel
import org.hyt.hytport.audio.api.service.HYTBinder
import org.hyt.hytport.graphics.factory.HYTGLFactory
import org.hyt.hytport.util.HYTMathUtil
import org.hyt.hytport.util.HYTUtil
import org.hyt.hytport.visual.api.model.HYTPopState
import org.hyt.hytport.visual.api.model.HYTState
import org.hyt.hytport.visual.component.library.library
import org.hyt.hytport.visual.component.loading.loadingIcon
import org.hyt.hytport.visual.component.surface.pop
import org.hyt.hytport.visual.component.surface.rememberPopState
import org.hyt.hytport.visual.component.surface.surface
import org.hyt.hytport.visual.factory.HYTStateFactory
import java.util.*
import java.util.concurrent.ScheduledExecutorService

class HYTLibrary : HYTBaseActivity() {

    companion object {

        private val _STATES: Int = 6;

    }

    private lateinit var _canvas: GLSurfaceView.Renderer;

    private lateinit var _consumer: (ByteArray) -> Unit;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        val speed: Float = 60.0f / _rate;
        val pulseStates: Array<HYTState> = Array(_STATES) {
            HYTStateFactory.getPulseState(0.01f * speed);
        };
        _consumer = { food: ByteArray ->
            val result: Float = HYTMathUtil.getNormalBytesAverage(food);
            val chosen: Int = (Math.random() * _STATES).toInt();
            if (result > 0.02) {
                pulseStates[chosen].setState(result);
            }
        }
        _canvas = HYTGLFactory.getCanvas(
            this,
            HYTUtil.readSource(resources.getString(R.string.library_vertex_shader), assets),
            HYTUtil.readSource(resources.getString(R.string.library_shader), assets),
            mapOf(
                Pair(resources.getString(R.string.pulse_states), pulseStates)
            )
        );
    }

    @Composable
    override fun compose(player: HYTBinder, executor: ScheduledExecutorService) {
        val context: Context = LocalContext.current;
        DisposableEffect(player) {
            val auditor: HYTBinder.Companion.HYTAuditor =
                object : HYTBinder.Companion.HYTAuditor {

                    override fun consumer(food: ByteArray) {
                        _consumer(food);
                    }

                };
            player.addAuditor(auditor);
            onDispose {
                player.removeAuditor(auditor);
            }
        }
        var instructor: Boolean by remember {
            mutableStateOf(
                _preferences.getBoolean(
                    resources.getString(R.string.preferences_library_instructor),
                    true
                )
            )
        };

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = colorResource(R.color.hyt_dark))
        ) {
            val actions: Queue<() -> Unit> = remember { LinkedList() };
            val popState: HYTPopState = rememberPopState(
                remember { "Library" },
                remember { "Next" },
                buildAnnotatedString {
                    append("Here you can manage the playback order")
                }
            );
            val reorderTip: Painter = painterResource(R.drawable.hyt_instructor_reorder_200dp);
            val queueTip: Painter = painterResource(R.drawable.hyt_instructor_add_to_queue_200dp);
            DisposableEffect(popState) {
                val auditor: HYTPopState.Companion.HYTAuditor = object :
                    HYTPopState.Companion.HYTAuditor {

                    override fun onAccept() {
                        val action: (() -> Unit)? = actions.poll();
                        if (action == null) {
                            _preferences
                                .edit()
                                .putBoolean(
                                    resources.getString(R.string.preferences_library_instructor),
                                    false
                                )
                                .apply();
                            instructor = false;
                        } else {
                            action();
                        }
                    }

                };
                popState.addAuditor(auditor);
                onDispose {
                    popState.removeAuditor(auditor);
                }
            }
            val popStateEffect: HYTPopState by rememberUpdatedState(popState);
            val white: Color = colorResource(R.color.hyt_white);
            LaunchedEffect(instructor) {
                if (instructor) {
                    actions.offer {
                        popStateEffect.content(
                            buildAnnotatedString {
                                append("Hold track and move it to reorder")
                            }
                        );
                        popStateEffect.image(reorderTip)
                    };
                    actions.offer {
                        popStateEffect.content(
                            buildAnnotatedString {
                                append("Drag and drop track into other queues while holding album cover")
                            }
                        )
                        popStateEffect.image(queueTip)
                    }
                    actions.offer {
                        popState.content(
                            buildAnnotatedString {
                                pushStyle(
                                    SpanStyle(
                                        color = white,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                )
                                append("- swipe track to remove from the queue\n")
                                append("- hold queue to start editing\n")
                                pop();
                                append("Tracks from mainstream cannot be permanently removed, instead they are placed into the hidden queue and vice versa. Also, these queues cannot be changed or removed")
                            }
                        );
                        popState.confirm("OK");
                    }
                }
            }
            val instructorAnimation: Float by animateFloatAsState(if (instructor) 1.0f else 0.0f);
            if (instructor || instructorAnimation > 0.0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = colorResource(R.color.hyt_dark)
                        )
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .alpha(instructorAnimation)
                ) {
                    pop(
                        state = popState
                    );
                }
            } else if (instructorAnimation < 1.0f) {
                surface(
                    renderer = _canvas,
                    paused = false
                );
                library(
                    player = player,
                    click = { audio: HYTAudioModel ->
                        player.play(audio);
                    },
                    back = {
                        finish();
                    },
                    executor = executor,
                    modifier = Modifier
                        .alpha(1.0f - instructorAnimation)
                );
            }
        }
    }

}