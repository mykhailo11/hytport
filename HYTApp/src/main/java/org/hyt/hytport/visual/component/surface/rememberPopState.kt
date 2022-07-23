package org.hyt.hytport.visual.component.surface

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.AnnotatedString
import org.hyt.hytport.visual.api.model.HYTPopState
import org.hyt.hytport.visual.factory.HYTComponentFactory

@Composable
fun rememberPopState(
    initialTitle: String,
    initialConfirm: String,
    initialContent: AnnotatedString,
    initialPainter: Painter? = null
) : HYTPopState {
    val state: HYTPopState by remember(
        initialTitle,
        initialConfirm,
        initialContent,
        initialPainter
    ) {
        derivedStateOf {
            HYTComponentFactory.getPopState(
                initialTitle = initialTitle,
                initialConfirm = initialConfirm,
                initialContent = initialContent,
                initialPainter = initialPainter
            )
        }
    }
    return state;
}