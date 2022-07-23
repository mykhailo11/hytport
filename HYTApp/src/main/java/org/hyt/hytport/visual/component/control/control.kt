package org.hyt.hytport.visual.component.control

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.hyt.hytport.R
import org.hyt.hytport.audio.api.model.HYTAudioModel
import org.hyt.hytport.audio.api.service.HYTBinder
import org.hyt.hytport.visual.api.model.HYTControlState
import org.hyt.hytport.visual.component.util.pressed

@Composable
fun control(
    player: HYTBinder,
    modifier: Modifier = Modifier,
    longClick: () -> Unit
) {
    val controlState: HYTControlState = rememberControlState(
        next = painterResource(R.drawable.hyt_next_200dp),
        nextActive = painterResource(R.drawable.hyt_next_press_200dp),
        previous = painterResource(R.drawable.hyt_next_200dp),
        previousActive = painterResource(R.drawable.hyt_next_press_200dp),
        play = painterResource(R.drawable.hyt_pause_200dp),
        playActive = painterResource(R.drawable.hyt_play_200dp),
        playPress = painterResource(R.drawable.hyt_play_press_200dp)
    );
    val auditor: HYTBinder.Companion.HYTAuditor by remember(player) {
        derivedStateOf {
            object : HYTBinder.Companion.HYTAuditor {

                override fun onPlay(audio: HYTAudioModel, current: Long) {
                    controlState.play();
                }

                override fun onPause(audio: HYTAudioModel, current: Long) {
                    controlState.pause();
                }

                override fun onReady(audio: HYTAudioModel?, current: Long) {
                    player.isPlaying { playing: Boolean ->
                        if (playing) {
                            controlState.play();
                        } else {
                            controlState.pause();
                        }
                    }
                }

                override fun onDestroy() {
                    controlState.pause();
                }

                override fun onNext(audio: HYTAudioModel) {
                    controlState.play();
                }

                override fun onPrevious(audio: HYTAudioModel) {
                    controlState.play();
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
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround,
        modifier = Modifier
            .then(modifier)
            .sizeIn(
                maxHeight = 120.dp,
                maxWidth = 500.dp
            )
    ) {
        Image(
            painter = controlState.previousButton(),
            contentScale = ContentScale.Fit,
            contentDescription = null,
            modifier = Modifier
                .rotate(180f)
                .weight(1.0f)
                .height(70.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = pressed(
                            pressed = {
                                controlState.previous(true);
                                player.previous();
                            },
                            react = {
                                controlState.previous(false);
                            }
                        )
                    )
                }
        );
        Image(
            painter = controlState.playButton(),
            contentScale = ContentScale.Fit,
            contentDescription = null,
            modifier = Modifier
                .weight(1.0f)
                .height(70.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = pressed(
                            pressed = {
                                controlState.press(true);
                            },
                            react = {
                                controlState.press(false);
                            }
                        ),
                        onLongPress = {
                            longClick()
                        },
                        onTap = {
                            player.isPlaying { playing: Boolean ->
                                if (playing) {
                                    player.pause();
                                } else {
                                    player.play();
                                }
                            }
                        }
                    )
                }
        );
        Image(
            painter = controlState.nextButton(),
            contentScale = ContentScale.Fit,
            contentDescription = null,
            modifier = Modifier
                .weight(1.0f)
                .height(70.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = pressed(
                            pressed = {
                                controlState.next(true);
                                player.next();
                            },
                            react = {
                                controlState.next(false);
                            }
                        )
                    )
                }
        );
    }
}