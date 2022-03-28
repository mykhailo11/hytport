package org.hyt.hytport.audio.api.service

import android.os.IBinder
import org.hyt.hytport.audio.api.access.HYTAudioRepository
import org.hyt.hytport.audio.api.model.HYTAudioModel

interface HYTBinder: HYTAudioPlayer, IBinder {

    companion object {

        interface HYTAuditor {

            fun getId(): Long;

            fun setId(id: Long): Unit;

            fun onReady(): Unit{}

            fun onPlay(audio: HYTAudioModel): Unit{}

            fun onPause(audio: HYTAudioModel): Unit{}

            fun onNext(audio: HYTAudioModel): Unit{}

            fun onAddNext(audio: HYTAudioModel): Unit{}

            fun onPrevious(audio: HYTAudioModel): Unit{}

            fun consumer(food: ByteArray): Unit{}

            fun progress(time: Int): (Int) -> Unit{
                return {}
            }

            fun onRepositoryChanged(repository: HYTAudioRepository): Unit{}

            fun onDestroy(): Unit{}

        }

    }

    fun addAuditor(auditor: HYTAuditor): Unit;

    fun removeAuditor(auditor: HYTAuditor): Unit;

    fun setRepository(repository: HYTAudioRepository): Unit;

    fun getRepository(): Class<HYTAudioRepository>?;

}