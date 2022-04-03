package org.hyt.hytport.visual.service

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import org.hyt.hytport.R
import org.hyt.hytport.audio.api.model.HYTAudioModel
import org.hyt.hytport.audio.api.service.HYTBinder
import org.hyt.hytport.audio.model.HYTBaseAudioModel
import org.hyt.hytport.visual.component.library.library
import org.hyt.hytport.visual.component.loading.loadingIcon
import java.util.*

class HYTLibrary : HYTBaseActivity() {

    @Composable
    override fun compose(player: HYTBinder) {
        val queue: List<HYTAudioModel>? by produceState(
            initialValue = null as List<HYTAudioModel>?,
            player
        ) {
            player.queue { actualQueue: Deque<HYTAudioModel> ->
                value = actualQueue.toList();
            }
        };
        var current: Long by remember { mutableStateOf(-1L) };
        val auditor: HYTBinder.Companion.HYTAuditor by remember(player) {
            derivedStateOf {
                object : HYTBinder.Companion.HYTAuditor {

                    override fun onReady(audio: HYTAudioModel) {
                        current = audio.getId();
                    }

                    override fun onNext(audio: HYTAudioModel) {
                        current = audio.getId();
                    }

                    override fun onPrevious(audio: HYTAudioModel) {
                        current = audio.getId();
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
                .background(
                    brush = Brush.linearGradient(
                        Pair(0.0f, colorResource(R.color.hyt_accent_dark)),
                        Pair(0.1f, colorResource(R.color.hyt_dark)),
                        Pair(0.8f, colorResource(R.color.hyt_dark)),
                        Pair(1.0f, colorResource(R.color.hyt_accent_dark))
                    )
                )
        ) {
            library(
                libraryItems = queue,
                current,
                click = { audio: HYTAudioModel ->
                    player.play(audio);
                },
                back = {
                    finish();
                }
            );
        }
    }

}