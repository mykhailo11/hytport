package org.hyt.hytport.audio.api.service

import org.hyt.hytport.audio.api.model.HYTAudioModel
import java.util.*

interface HYTAudioPlayer {

    fun play(): HYTAudioModel;

    fun play(audio: HYTAudioModel): HYTAudioModel;

    fun isPlaying(): Boolean;

    fun pause(): HYTAudioModel;

    fun next(): HYTAudioModel;

    fun previous(): HYTAudioModel;

    fun addNext(next: HYTAudioModel);

    fun queue(consumer: (Deque<HYTAudioModel>) -> Unit): Unit;

    fun consumer(consumer: (ByteArray) -> Unit): Unit;

    fun progress(consumer: (Int) -> (Int) -> Unit): Unit;

    fun seek(to: Int): Unit;

    fun destroy();

}