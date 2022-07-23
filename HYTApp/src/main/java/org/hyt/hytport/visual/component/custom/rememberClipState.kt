package org.hyt.hytport.visual.component.custom

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import org.hyt.hytport.visual.api.model.HYTClipState
import org.hyt.hytport.visual.factory.HYTComponentFactory

@Composable
fun rememberClipState(): HYTClipState {
    val state: HYTClipState by remember {
        derivedStateOf {
            HYTComponentFactory.getClipState();
        }
    };
    return state;
}