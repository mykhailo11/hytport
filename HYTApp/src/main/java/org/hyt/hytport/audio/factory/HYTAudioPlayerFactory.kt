package org.hyt.hytport.audio.factory

import android.content.Context
import org.hyt.hytport.audio.api.service.HYTAudioPlayer
import org.hyt.hytport.audio.service.HYTBaseAudioPlayer

class HYTAudioPlayerFactory {

    companion object{

        fun getAudioPlayer(context: Context, completion: () -> Unit): HYTAudioPlayer {
            return HYTBaseAudioPlayer(context, completion);
        }

    }

}