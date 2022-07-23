package org.hyt.hytport.visual.component.player

import android.app.Activity
import android.content.Intent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hyt.hytport.R
import org.hyt.hytport.audio.api.model.HYTAudioManager
import org.hyt.hytport.audio.api.model.HYTAudioModel
import org.hyt.hytport.audio.api.service.HYTBinder
import org.hyt.hytport.visual.api.model.HYTPlayerScrollState
import org.hyt.hytport.visual.api.model.HYTPlayerState
import org.hyt.hytport.visual.component.control.control
import org.hyt.hytport.visual.component.custom.slider
import org.hyt.hytport.visual.component.montserrat
import org.hyt.hytport.visual.service.HYTSettings

@Composable
fun player(
    player: HYTBinder,
    playerState: HYTPlayerState = rememberPlayerState(),
    modifier: Modifier = Modifier,
    activity: Activity
) {
    var currentManager: HYTAudioManager? by remember { mutableStateOf(null) };
    var show: Boolean by rememberSaveable { mutableStateOf(false) };
    LaunchedEffect(player) {
        player.manager { manager: HYTAudioManager ->
            currentManager = manager;
        }
    }
    val playerScrollState: HYTPlayerScrollState = rememberPlayerScrollState();
    LaunchedEffect(currentManager) {
        currentManager?.shuffle { shuffle: Boolean ->
            playerState.shuffle(shuffle);
        }
        currentManager?.loop { loop: Boolean ->
            playerState.loop(loop);
        }
    }
    val auditor: HYTBinder.Companion.HYTAuditor by remember(player) {
        derivedStateOf {
            object : HYTBinder.Companion.HYTAuditor {

                override fun onReady(audio: HYTAudioModel?, current: Long) {
                    _setMeta(audio);
                    playerScrollState.slider(current / 1000.0f);
                    playerScrollState.max((audio?.getDuration() ?: 1L) / 1000.0f);
                }

                override fun onNext(audio: HYTAudioModel) {
                    _setMeta(audio);
                }

                override fun onPrevious(audio: HYTAudioModel) {
                    _setMeta(audio);
                }

                override fun onSetManager(manager: HYTAudioManager, audio: HYTAudioModel?) {
                    currentManager = manager;
                    _setMeta(audio);
                }

                override fun progress(duration: Int, current: Int) {
                    if (!playerScrollState.sliding()) {
                        playerScrollState.slider(current / 1000.0f);
                        playerScrollState.max(duration / 1000.0f);
                    }
                }

                private fun _setMeta(
                    audio: HYTAudioModel?
                ): Unit {
                    val audioArtist: String? = audio?.getArtist();
                    val audioTitle: String? = audio?.getTitle();

                    if (audioArtist != null) {
                        playerState.artist(audioArtist);
                    }
                    if (audioTitle != null) {
                        playerState.title(audioTitle);
                    }
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
    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .then(modifier)
            .sizeIn(
                maxHeight = 300.dp,
                maxWidth = 500.dp
            )
            .fillMaxHeight()
    ) {
        var height: Int by remember { mutableStateOf(0) };
        val animation by animateFloatAsState(
            if (playerState.show()) 0.0f
            else 1.0f
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .graphicsLayer(
                    translationY = 0.5f * height * animation
                )
        ) {
            Text(
                text = playerState.title(),
                fontFamily = montserrat,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.hyt_white),
                fontSize = 25.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(
                        state = playerScrollState.titleScroll()
                    )
            );
            Text(
                text = playerState.artist(),
                fontFamily = montserrat,
                color = colorResource(R.color.hyt_text_dark),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(
                        state = playerScrollState.artistScroll()
                    )
            );
        }
        val currentManagerEffect: HYTAudioManager? by rememberUpdatedState(currentManager);
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates: LayoutCoordinates ->
                    height = coordinates.size.height;
                }
                .alpha(1.0f - animation)
        ) {
            Image(
                painter = painterResource(
                    if (playerState.shuffle()) {
                        R.drawable.hyt_shuffle_active_200dp
                    } else {
                        R.drawable.hyt_shuffle_200dp
                    }
                ),
                contentDescription = null,
                alignment = Alignment.Center,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .height(30.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                if (playerState.show()) {
                                    currentManagerEffect?.shuffle(!playerState.shuffle());
                                    currentManagerEffect?.shuffle { shuffle: Boolean ->
                                        playerState.shuffle(shuffle);
                                    }
                                }
                            }

                        )
                    }
            );
            Image(
                painter = painterResource(
                    if (playerState.loop()) {
                        R.drawable.hyt_loop_active_200dp
                    } else {
                        R.drawable.hyt_loop_200dp
                    }
                ),
                contentDescription = null,
                alignment = Alignment.Center,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .height(30.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                if (playerState.show()) {
                                    currentManagerEffect?.loop(!playerState.loop());
                                    currentManagerEffect?.loop { loop: Boolean ->
                                        playerState.loop(loop);
                                    }
                                }
                            }

                        )
                    }
            );
            Image(
                painter = painterResource(R.drawable.hyt_settings_200dp),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .requiredHeight(25.dp)
                    .aspectRatio(
                        ratio = 1.0f
                    )
                    .clickable {
                        activity.startActivityIfNeeded(Intent(activity, HYTSettings::class.java), 100);
                    }
            );
        }
        slider(
            state = playerScrollState,
            showTime = playerState.show(),
            seek = { to: Int ->
                player.seek(to);
            },
            modifier = Modifier
                .graphicsLayer(
                    translationY = -height * 0.5f * animation
                )
        );
        control(
            player = player,
            longClick = {
                show = !show;
                playerState.show(show);
            },
            modifier = Modifier
                .graphicsLayer(
                    translationY = -height * 0.5f * animation
                )
        );
    }
}
