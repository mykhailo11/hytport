package org.hyt.hytport.visual.component.control

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.Painter
import org.hyt.hytport.visual.api.model.HYTControlState
import org.hyt.hytport.visual.factory.HYTComponentFactory

@Composable
fun rememberControlState(
    playing: Boolean = false,
    next: Painter,
    nextActive: Painter,
    previous: Painter,
    previousActive: Painter,
    play: Painter,
    playActive: Painter,
    playPress: Painter
): HYTControlState {
    return remember {
        HYTComponentFactory.getControlState(
            playing = playing,
            next = next,
            nextActive = nextActive,
            previous = previous,
            previousActive = previousActive,
            play = play,
            playActive = playActive,
            playPress = playPress
        );
    }
}