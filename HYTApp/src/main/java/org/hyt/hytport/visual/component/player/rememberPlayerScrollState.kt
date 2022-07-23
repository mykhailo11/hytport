package org.hyt.hytport.visual.component.player

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.hyt.hytport.visual.api.model.HYTPlayerScrollState
import org.hyt.hytport.visual.factory.HYTComponentFactory

@Composable
fun rememberPlayerScrollState(
    slider: Float = 0.0f,
    max: Float = 10.0f,
): HYTPlayerScrollState {
    val artistScroll: ScrollState = rememberScrollState();
    val titleScroll: ScrollState = rememberScrollState();
    return remember {
        HYTComponentFactory.getPlayerScrollState(
            slider = slider,
            max = max,
            artistScroll = artistScroll,
            titleScroll = titleScroll
        );
    };
}