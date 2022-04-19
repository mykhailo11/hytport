package org.hyt.hytport.audio.api.service

import org.hyt.hytport.audio.api.model.HYTAudioManager
import org.hyt.hytport.audio.api.model.HYTAudioModel

interface HYTAudioPlayer {

    companion object {

        interface HYTAuditor {

            fun onReady(audio: HYTAudioModel): Unit{}

            fun onPlay(audio: HYTAudioModel, current: Long): Unit{}

            fun onPause(audio: HYTAudioModel, current: Long): Unit{}

            fun onNext(audio: HYTAudioModel): Unit{}

            fun onPrevious(audio: HYTAudioModel): Unit{}

            fun onComplete(audio: HYTAudioModel): Unit{}

            fun progress(duration: Int, current: Int): Unit{}

            fun onSetManager(manager: HYTAudioManager): Unit {}

            fun onDestroy(): Unit{}

        }

    }

    fun play(): Unit;

    fun play(audio: HYTAudioModel): Unit;

    fun isPlaying(consumer: (Boolean) -> Unit): Unit;

    fun pause(): Unit;

    fun next(): Unit;

    fun previous(): Unit;

    fun seek(to: Int): Unit;

    fun destroy(): Unit;

    fun manger(
        empty: (() -> Unit)? = null,
        consumer: (HYTAudioManager) -> Unit
    ): Unit;

    fun setManager(manager: HYTAudioManager): Unit;

    fun setAuditor(auditor: HYTAuditor): Unit;

    fun resetAuditor(): Unit;

}