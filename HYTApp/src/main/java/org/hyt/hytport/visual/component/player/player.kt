package org.hyt.hytport.visual.component.player

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hyt.hytport.R
import org.hyt.hytport.audio.api.model.HYTAudioModel
import org.hyt.hytport.audio.api.service.HYTBinder
import org.hyt.hytport.visual.component.control.control

@Composable
fun player(
    player: HYTBinder,
    modifier: Modifier = Modifier
) {
    var slider: Float by remember { mutableStateOf(0.0f) };
    var sliding: Boolean by remember { mutableStateOf(false) };
    var sliderMax: Float by remember { mutableStateOf(10.0f) };
    var artist: String by remember { mutableStateOf("Artist") };
    val artistScroll = rememberScrollState();
    var title: String by remember { mutableStateOf("Title") };
    val titleScroll = rememberScrollState();
    val auditor: HYTBinder.Companion.HYTAuditor by remember(player) {
        derivedStateOf {
            object : HYTBinder.Companion.HYTAuditor {

                override fun onReady(audio: HYTAudioModel) {
                    _setMeta(audio);
                }

                override fun onNext(audio: HYTAudioModel) {
                    _setMeta(audio);
                }

                override fun onPrevious(audio: HYTAudioModel) {
                    _setMeta(audio);
                }

                override fun progress(duration: Int, current: Int) {
                    if (!sliding) {
                        slider = current / 1000.0f;
                        sliderMax = duration / 1000.0f;
                    }
                }

                private fun _setMeta(
                    audio: HYTAudioModel
                ): Unit {
                    val audioArtist: String? = audio.getArtist();
                    val audioTitle: String? = audio.getTitle();
                    if (audioArtist != null) {
                        artist = audioArtist;
                    }
                    if (audioTitle != null) {
                        title = audioTitle;
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
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = colorResource(R.color.hyt_dark)
            )
            .padding(50.dp)
            .then(modifier)
    ) {
        Column {
            Text(
                text = title,
                color = colorResource(R.color.hyt_grey),
                fontSize = 28.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(
                        state = titleScroll
                    )
            );
            Text(
                text = artist,
                color = colorResource(R.color.hyt_text_dark),
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(
                        state = artistScroll
                    )
            );
        }
        Slider(
            value = slider,
            enabled = true,
            onValueChange = {
                if (!sliding) {
                    sliding = true;
                }
                slider = it
            },
            onValueChangeFinished = {
                sliding = false;
                player.seek((slider * 1000.0f).toInt())
            },
            colors = SliderDefaults.colors(
                thumbColor = colorResource(R.color.hyt_accent),
                activeTrackColor = colorResource(R.color.hyt_grey)
            ),
            valueRange = 0.0f..sliderMax
        );
        control(
            player = player
        );
    }
}
