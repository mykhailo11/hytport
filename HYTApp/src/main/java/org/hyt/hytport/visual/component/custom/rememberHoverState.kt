package org.hyt.hytport.visual.component.custom

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import org.hyt.hytport.visual.api.model.HYTHoverState
import org.hyt.hytport.visual.factory.HYTComponentFactory

@Composable
fun rememberHoverState(): HYTHoverState {
    val state: HYTHoverState by remember {
        derivedStateOf {
            HYTComponentFactory.getHoverState();
        }
    }
    return state;
}