package org.hyt.hytport.visual.service

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.colorResource
import org.hyt.hytport.R
import org.hyt.hytport.audio.api.model.HYTAudioModel
import org.hyt.hytport.audio.api.service.HYTBinder
import org.hyt.hytport.visual.component.library.library
import org.hyt.hytport.visual.component.loading.loadingIcon
import java.util.concurrent.ScheduledExecutorService

class HYTLibrary : HYTBaseActivity() {

    @Composable
    override fun compose(player: HYTBinder, executor: ScheduledExecutorService) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        Pair(0.8f, colorResource(R.color.hyt_dark)),
                        Pair(1.0f, colorResource(R.color.hyt_accent_dark))
                    )
                )
        ) {
            library(
                player = player,
                click = { audio: HYTAudioModel ->
                    player.play(audio);
                },
                back = {
                    finish();
                },
                executor = executor
            );
        }
    }

    @Composable
    override fun loading() {
        loadingIcon();
    }
}