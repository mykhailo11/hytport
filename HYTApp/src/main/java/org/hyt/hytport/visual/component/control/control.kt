package org.hyt.hytport.visual.component.control

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import org.hyt.hytport.R
import org.hyt.hytport.audio.api.model.HYTAudioModel
import org.hyt.hytport.audio.api.service.HYTBinder
import org.hyt.hytport.visual.component.util.pressed
import java.util.*

@Composable
fun control(
    player: HYTBinder,
    modifier: Modifier = Modifier
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
            contentScale = ContentScale.FillBounds,
            contentDescription = null,
            modifier = Modifier
                .rotate(180f)
                .weight(1.0f)
                .aspectRatio(1.0f)
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
            contentScale = ContentScale.FillBounds,
            contentDescription = null,
            modifier = Modifier
                .weight(1.0f)
                .aspectRatio(1.0f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    if (playing) {
                        player.pause();
                    } else {
                        player.play();
                    }
                }
        );
        Image(
            painter = painterResource(
                if (nextClicked)
                    R.drawable.hyt_next_press_200dp
                else
                    R.drawable.hyt_next_200dp
            ),
            contentScale = ContentScale.FillBounds,
            contentDescription = null,
            modifier = Modifier
                .weight(1.0f)
                .aspectRatio(1.0f)
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