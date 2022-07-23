package org.hyt.hytport.visual.component.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import org.hyt.hytport.visual.api.model.HYTSettingState
import org.hyt.hytport.visual.factory.HYTComponentFactory

@Composable
fun rememberSettingState(
    initial: Boolean = false,
    initialTitle: String,
    initialDescription: String
): HYTSettingState {
    val state: HYTSettingState by remember(
        initial,
        initialTitle,
        initialDescription
    ) {
        derivedStateOf {
            HYTComponentFactory.getSettingState(
                initial = initial,
                initialTitle = initialTitle,
                initialDescription = initialDescription
            )
        }
    };
    return state;
}