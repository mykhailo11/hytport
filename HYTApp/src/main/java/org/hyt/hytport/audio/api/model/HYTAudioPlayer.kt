package org.hyt.hytport.audio.api.model

import android.content.res.AssetFileDescriptor
import android.net.Uri
import java.util.*

interface HYTAudioPlayer {

    interface HYTAudioPlayerAudit{

        fun onReady(): Unit{}

        fun onPlay(audio: HYTAudioModel): Unit{}

        fun onPause(audio: HYTAudioModel): Unit{}

        fun onNext(audio: HYTAudioModel): Unit{}

        fun onPrevious(audio: HYTAudioModel): Unit{}

        fun consumer(food: ByteArray): Unit{}

        fun onAddNext(audio: HYTAudioModel): Unit{}

        fun onDestroy(audio: HYTAudioModel): Unit{}

    }

    fun play(): HYTAudioModel;

    fun play(audio: HYTAudioModel): HYTAudioModel;

    fun isPlaying(): Boolean;

    fun pause(): HYTAudioModel;

    fun next(): HYTAudioModel;

    fun previous(): HYTAudioModel;

    fun addAudit(audit: HYTAudioPlayerAudit): Int;

    fun removeAudit(audit: Int): Unit;

    fun addNext(next: HYTAudioModel);

    fun queue(): Deque<HYTAudioModel>;

    fun destroy();

}