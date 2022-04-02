package org.hyt.hytport.visual.component.player

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLSurfaceView
import android.os.Binder
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hyt.hytport.R
import org.hyt.hytport.audio.api.model.HYTAudioModel
import org.hyt.hytport.audio.api.service.HYTAudioPlayer
import org.hyt.hytport.audio.api.service.HYTBinder
import org.hyt.hytport.graphics.factory.HYTGLFactory
import org.hyt.hytport.graphics.service.HYTCanvas
import org.hyt.hytport.util.HYTUtil
import org.hyt.hytport.visual.component.control.control
import org.hyt.hytport.visual.component.surface.surface
import org.hyt.hytport.visual.component.util.binder

@Composable
fun player(
    player: HYTBinder,
    modifier: Modifier = Modifier
) {
    var slider: Float by remember { mutableStateOf(0.0f) };
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
                    slider = current / 1000.0f;
                    sliderMax = duration / 1000.0f;
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
            onValueChange = { slider = it },
            onValueChangeFinished = {
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

@Preview(showBackground = true)
@Composable
fun playerPreview() {
    player(binder);
}
