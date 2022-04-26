package org.hyt.hytport.audio.factory

import org.hyt.hytport.audio.access.HYTDatabase
import org.hyt.hytport.audio.api.access.HYTAudioRepository
import org.hyt.hytport.audio.api.model.HYTAudioManager
import org.hyt.hytport.audio.api.model.HYTAudioModel
import org.hyt.hytport.audio.api.service.HYTQueueProvider
import org.hyt.hytport.audio.model.HYTBaseAudioManager
import org.hyt.hytport.audio.service.HYTBaseQueueProvider

class HYTQueueFactory {

    companion object {

        fun getManager(queue: MutableList<HYTAudioModel>): HYTAudioManager {
            return HYTBaseAudioManager(queue);
        }

        fun getQueueProvider(
            repository: HYTAudioRepository,
            database: HYTDatabase
        ): HYTQueueProvider {
            return HYTBaseQueueProvider(
                repository,
                database
            );
        }

    }

}