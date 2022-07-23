package org.hyt.hytport.visual.api.model

import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.AnnotatedString

interface HYTPopState {

    companion object {

        interface HYTAuditor {

            fun onClick(): Unit {}

            fun onAccept(): Unit {}

        }

    }

    fun title(): String;

    fun title(title: String): Unit;

    fun content(): AnnotatedString;

    fun content(content: AnnotatedString): Unit;

    fun click(): Unit;

    fun accept(): Unit;

    fun image(): Painter?;

    fun image(painter: Painter?);

    fun confirm(): String;

    fun confirm(confirm: String);

    fun addAuditor(auditor: HYTAuditor): Unit;

    fun removeAuditor(auditor: HYTAuditor): Unit;

}