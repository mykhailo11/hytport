package org.hyt.hytport.visual.api.model

interface HYTPlayerState {

    fun shuffle(): Boolean;

    fun shuffle(shuffle: Boolean): Unit;

    fun loop(): Boolean;

    fun loop(loop: Boolean): Unit;

    fun artist(): String;

    fun artist(artist: String): Unit;

    fun title(): String;

    fun title(title: String): Unit;

    fun show(): Boolean;

    fun lock(lock: Boolean): Unit;

    fun show(show: Boolean): Unit;

}