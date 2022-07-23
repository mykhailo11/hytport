package org.hyt.hytport.visual.component.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import org.hyt.hytport.visual.api.model.HYTPlayerState
import org.hyt.hytport.visual.factory.HYTComponentFactory

@Composable
fun rememberPlayerState(
    artist: String = "Artist",
    title: String = "Title",
): HYTPlayerState {
    val state: HYTPlayerState by remember(artist, title) {
        derivedStateOf {
            HYTComponentFactory.getPlayerState(
                artist,
                title
            );
        }
    }
    return state
}