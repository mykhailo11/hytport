package org.hyt.hytport.audio.api.service

import org.hyt.hytport.audio.api.model.HYTAudioManager
import org.hyt.hytport.audio.api.model.HYTAudioModel

interface HYTQueueProvider {

    suspend fun getAll(consumer: (List<String>) -> Unit): Unit;

    suspend fun getByName(name: String, consumer: (HYTAudioManager) -> Unit): Unit;

    suspend fun mainstream(consumer: (List<HYTAudioModel>) -> Unit): Unit;

    suspend fun save(
        name: String,
        manager: HYTAudioManager,
        saved: ((HYTAudioManager) -> Unit)? = null
    ): Unit;

}