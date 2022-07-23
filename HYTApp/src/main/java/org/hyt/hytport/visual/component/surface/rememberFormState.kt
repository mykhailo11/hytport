package org.hyt.hytport.visual.component.surface

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import org.hyt.hytport.visual.api.model.HYTFormState
import org.hyt.hytport.visual.factory.HYTComponentFactory

@Composable
fun rememberFormState(
    initialTitle: String,
    initialConfirm: String
): HYTFormState {
    val state: HYTFormState by remember(initialTitle, initialConfirm) {
        derivedStateOf {
            HYTComponentFactory.getFormState(initialTitle, initialConfirm);
        }
    };
    return state;
}