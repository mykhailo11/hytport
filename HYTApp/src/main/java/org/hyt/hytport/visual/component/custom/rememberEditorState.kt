package org.hyt.hytport.visual.component.custom

import androidx.compose.runtime.*
import org.hyt.hytport.visual.api.model.HYTEditorState
import org.hyt.hytport.visual.factory.HYTComponentFactory

@Composable
fun rememberEditorState(
    initialValue: String,
    label: String,
    enabled: Boolean = false
): HYTEditorState {
    val state: HYTEditorState by remember(
        initialValue,
        label
    ) {
        derivedStateOf {
            HYTComponentFactory.getEditorState(initialValue, label);
        }
    };
    LaunchedEffect(enabled) {
        state.enabled(enabled);
    }
    return state;
}