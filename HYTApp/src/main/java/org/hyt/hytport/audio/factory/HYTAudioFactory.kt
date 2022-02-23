package org.hyt.hytport.audio.factory

import android.content.ContentResolver
import android.content.Context
import org.hyt.hytport.audio.api.model.HYTAudioModel
import org.hyt.hytport.audio.api.model.HYTAudioRepository
import org.hyt.hytport.audio.model.HYTBaseAudioModel
import org.hyt.hytport.audio.model.HYTBaseAudioRepository
import org.hyt.hytport.audio.model.HYTRemoteAudioRepository

class HYTAudioFactory {

    companion object{

        fun getAudioModel(): HYTAudioModel{
            return HYTBaseAudioModel();
        }

        fun getAudioRepository(resolver: ContentResolver): HYTAudioRepository{
            return HYTBaseAudioRepository(resolver);
        }

        fun getRemoteAudioRepository(
            base: String,
            endpoints: Map<HYTRemoteAudioRepository.Companion.HYTEndpoints, String>,
            context: Context
        ): HYTAudioRepository{
            return HYTRemoteAudioRepository(base, endpoints, context);
        }

    }

}