package org.hyt.hytport.audio.api.model

interface HYTAudioManager {

    fun shuffle(shuffle: Boolean): Unit;

    fun shuffle(consumer: (Boolean) -> Unit);

    fun next(consumer: (HYTAudioModel) -> Unit): Unit;

    fun previous(consumer: (HYTAudioModel) -> Unit): Unit;

    fun current(audio: HYTAudioModel): Unit;

    fun current(consumer: (HYTAudioModel) -> Unit);

    fun queue(
        consumer: (MutableList<HYTAudioModel>) -> Unit
    ): Unit;

}