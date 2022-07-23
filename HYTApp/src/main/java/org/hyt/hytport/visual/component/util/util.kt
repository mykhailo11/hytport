package org.hyt.hytport.visual.component.util

import androidx.compose.foundation.gestures.PressGestureScope
import androidx.compose.ui.geometry.Offset

fun pressed(
    pressed: (Offset) -> Unit,
    react: (Boolean) -> Unit
): suspend PressGestureScope.(Offset) -> Unit {
    return { offset: Offset ->
        pressed(offset);
        val up = try {
            tryAwaitRelease()
        } catch (exception: Exception) {
            false
        };
        react(up);
    }
}