package org.hyt.hytport.visual.api.model

import org.hyt.hytport.audio.api.model.HYTAudioManager

interface HYTLibraryState {

    fun manager(): HYTAudioManager?;

    fun current(): Long;

    fun current(current: Long): Unit;

}