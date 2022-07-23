package org.hyt.hytport.visual.api.model

import androidx.compose.foundation.ScrollState

interface HYTPlayerScrollState {

    fun slider(): Float;

    fun slider(slide: Float);

    fun sliding(): Boolean;

    fun sliding(sliding: Boolean): Unit;

    fun max(): Float;

    fun max(max: Float): Unit;

    fun artistScroll(): ScrollState;

    fun titleScroll(): ScrollState;

}