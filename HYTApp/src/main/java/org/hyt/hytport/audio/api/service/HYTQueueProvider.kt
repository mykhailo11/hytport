package org.hyt.hytport.audio.api.service

import org.hyt.hytport.audio.api.model.HYTAudioManager
import org.hyt.hytport.audio.api.model.HYTAudioModel

interface HYTQueueProvider {

    suspend fun getAll(consumer: (List<String>) -> Unit): Unit;

    suspend fun getByName(name: String, consumer: (HYTAudioManager) -> Unit): Unit;

    suspend fun mainstream(consumer: (List<HYTAudioModel>) -> Unit): Unit;

    suspend fun new(consumer: (List<HYTAudioModel>) -> Unit): Unit;

    suspend fun save(
        manager: HYTAudioManager,
        saved: ((HYTAudioManager) -> Unit)? = null
    ): Unit;

    suspend fun save(
        name: String,
        vararg tracks: HYTAudioModel,
        saved: ((List<HYTAudioModel>) -> Unit)? = null
    ): Unit;

    suspend fun add(
        name: String,
        vararg tracks: HYTAudioModel,
        saved: ((List<HYTAudioModel>) -> Unit)? = null
    )

    suspend fun edit(
        name: String,
        new: String,
        saved: ((Boolean) -> Unit)? = null
    )

    suspend fun remove(
        name: String,
        saved: (() -> Unit)? = null
    );

}