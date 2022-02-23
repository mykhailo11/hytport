package org.hyt.hytport.visual.api.model

interface HYTState {

    fun setState(state: Float): Unit;

    fun getState(): Float;

}