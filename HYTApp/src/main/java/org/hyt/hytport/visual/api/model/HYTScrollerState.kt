package org.hyt.hytport.visual.api.model

import androidx.compose.foundation.gestures.ScrollableState

interface HYTScrollerState {

    fun offset(): Int;

    fun offset(offset: Int): Unit;

    fun scroll(delta: Float): Unit;

    fun onScroll(): Unit;

    fun barHeight(): Int;

    fun barHeight(height: Int): Unit;

    fun scrollerHeight(): Int;

    fun scrollerHeight(height: Int): Unit;

    fun totalUpdate(): Unit;

    fun scrollerScale(): Float;

    fun scroller(): ScrollableState?;

    fun scroller(scroller: ScrollableState): Unit;

    fun fastScroll(): Boolean;

    fun fastScroll(fast: Boolean): Unit;

    fun adjust(): Unit;

}