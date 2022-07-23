package org.hyt.hytport.visual.api.model

import androidx.compose.ui.graphics.painter.Painter

interface HYTControlState {

    fun play(): Unit;

    fun pause(): Unit;

    fun press(press: Boolean): Unit;

    fun next(hold: Boolean): Unit;

    fun previous(hold: Boolean): Unit;

    fun playButton(): Painter;

    fun nextButton(): Painter;

    fun previousButton(): Painter;

}