package org.hyt.hytport.visual.service

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import org.hyt.hytport.R
import org.hyt.hytport.audio.api.model.HYTAudioModel
import org.hyt.hytport.audio.api.service.HYTBinder
import org.hyt.hytport.graphics.factory.HYTGLFactory
import org.hyt.hytport.util.HYTMathUtil
import org.hyt.hytport.util.HYTUtil
import org.hyt.hytport.visual.api.model.HYTState
import org.hyt.hytport.visual.component.player.cover
import org.hyt.hytport.visual.component.player.player
import org.hyt.hytport.visual.component.surface.surface
import org.hyt.hytport.visual.factory.HYTStateFactory
import org.hyt.hytport.visual.old.service.HYTLibrary

class HYTApp : HYTBaseActivity() {

    companion object {

        private val _STATES: Int = 6;

    }

    private lateinit var _canvas: GLSurfaceView.Renderer;

    private lateinit var _consumer: (ByteArray) -> Unit;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        val pulseStates: Array<HYTState> = Array(_STATES) {
            HYTStateFactory.getPulseState(0.02f);
        };
        val balanceStates: Array<HYTState> = Array(_STATES) {
            HYTStateFactory.getBalanceState(0.02f);
        }
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
            )
        );
    }

    @Composable
    override fun compose(player: HYTBinder) {
        var visible: Boolean by remember { mutableStateOf(true) };
        val opacity: Float by animateFloatAsState(if (visible) 1.0f else 0.0f);
        var cover: Bitmap? by remember { mutableStateOf(null) };
        val context: Context = LocalContext.current;
        val auditor: HYTBinder.Companion.HYTAuditor by remember(player, context) {
            derivedStateOf {
                object : HYTBinder.Companion.HYTAuditor {

                    override fun consumer(food: ByteArray) {
                        _consumer(food);
                    }

                    override fun onComplete(audio: HYTAudioModel) {
                        player.next();
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
                },
                longClick = {
                    //startActivityIfNeeded(Intent(context, HYTLibrary::class.java), 100);
                }
            );
            val animating: Boolean = visible || opacity > 0.0f
            if (animating && LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                Row(
                    modifier = Modifier
                        .alpha(opacity)
                        .background(
                            color = colorResource(R.color.hyt_accent_light)
                        )
                ) {
                    cover(
                        album = cover
                    )
                    player(
                        player = player,
                        modifier = Modifier
                            .fillMaxSize()
                    );
                }
            } else if (animating) {
                Column(
                    modifier = Modifier
                        .alpha(opacity)
                        .background(
                            color = colorResource(R.color.hyt_accent_light)
                        )
                ) {
                    cover(
                        album = cover
                    )
                    player(
                        player = player,
                        modifier = Modifier
                            .fillMaxSize()
                    );
                }
            }
        }
    }

    override fun onDestroy() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super.onDestroy();
    }
}