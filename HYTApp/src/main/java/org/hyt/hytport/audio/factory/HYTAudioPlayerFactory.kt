package org.hyt.hytport.audio.factory

import android.content.Context
import org.hyt.hytport.audio.api.model.HYTAudioModel
import org.hyt.hytport.audio.api.service.HYTAudioPlayer
import org.hyt.hytport.audio.service.HYTBaseAudioPlayer
import java.util.*

class HYTAudioPlayerFactory {

    companion object{

        fun getAudioPlayer(
            context: Context,
            queueProvider: ((Deque<HYTAudioModel>) -> Unit) -> Unit
        ): HYTAudioPlayer {
            return HYTBaseAudioPlayer(queueProvider, context);
        }

    }

}