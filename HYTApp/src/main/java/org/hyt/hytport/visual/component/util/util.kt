package org.hyt.hytport.visual.component.util

import android.os.Binder
import androidx.compose.foundation.gestures.PressGestureScope
import androidx.compose.ui.geometry.Offset
import org.hyt.hytport.audio.api.model.HYTAudioModel
import org.hyt.hytport.audio.api.service.HYTAudioPlayer
import org.hyt.hytport.audio.api.service.HYTBinder

fun pressed(
    pressed: () -> Unit,
    react: (Boolean) -> Unit
): suspend PressGestureScope.(Offset) -> Unit {
    return {
        pressed();
        val up = try {
            tryAwaitRelease()
        } catch (exception: Exception) {
            false
        };
        react(up);
    }
}