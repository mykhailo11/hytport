package org.hyt.hytport.visual.component.control

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import org.hyt.hytport.R
import org.hyt.hytport.audio.api.model.HYTAudioModel
import org.hyt.hytport.audio.api.service.HYTBinder
import org.hyt.hytport.visual.component.util.pressed

@Composable
fun control(
    player: HYTBinder,
    modifier: Modifier = Modifier,
    longClick: () -> Unit
) {
    var playing: Boolean by remember(player) { mutableStateOf(false) };
    val auditor: HYTBinder.Companion.HYTAuditor by remember(player) {
        derivedStateOf {
            object : HYTBinder.Companion.HYTAuditor {

                override fun onPlay(audio: HYTAudioModel, current: Long) {
                    playing = true;
                }

                override fun onPause(audio: HYTAudioModel, current: Long) {
                    playing = false;
                }

                override fun onReady(audio: HYTAudioModel) {
                    player.isPlaying {
                        playing = it;
                    }
                }

                override fun onDestroy() {
                    playing = false;
                }

                override fun onNext(audio: HYTAudioModel) {
                    playing = true;
                }

                override fun onPrevious(audio: HYTAudioModel) {
                    playing = true;
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
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .then(modifier)
    ) {
        var previousClicked: Boolean by remember { mutableStateOf(false) };
        var nextClicked: Boolean by remember { mutableStateOf(false) };
        Image(
            painter = painterResource(
                if (previousClicked)
                    R.drawable.hyt_next_press_200dp
                else
                    R.drawable.hyt_next_200dp
            ),
            contentScale = ContentScale.Fit,
            contentDescription = null,
            modifier = Modifier
                .rotate(180f)
                .weight(1.0f)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = pressed(
                            pressed = {
                                previousClicked = true;
                                player.previous();
                            },
                            react = { up: Boolean ->
                                previousClicked = !up;
                            }
                        )
                    )
                }
        );
        Image(
            painter = painterResource(
                if (playing)
                    R.drawable.hyt_play_200dp
                else
                    R.drawable.hyt_pause_200dp
            ),
            contentScale = ContentScale.Fit,
            contentDescription = null,
            modifier = Modifier
                .weight(1.0f)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            longClick()
                        },
                        onTap = {
                            if (playing) {
                                player.pause();
                            } else {
                                player.play();
                            }
                        }
                    )
                }
        );
        Image(
            painter = painterResource(
                if (nextClicked)
                    R.drawable.hyt_next_press_200dp
                else
                    R.drawable.hyt_next_200dp
            ),
            contentScale = ContentScale.Fit,
            contentDescription = null,
            modifier = Modifier
                .weight(1.0f)
                .clickable {
                    player.next()
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = pressed(
                            pressed = {
                                nextClicked = true;
                                player.next();
                            },
                            react = { up: Boolean ->
                                nextClicked = !up;
                            }
                        )
                    )
                }
        );
    }
}