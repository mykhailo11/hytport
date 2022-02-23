package org.hyt.hytport.audio.factory

import android.content.Context
import android.provider.MediaStore
import org.hyt.hytport.audio.api.model.HYTAudioModel
import org.hyt.hytport.audio.api.model.HYTAudioPlayer
import org.hyt.hytport.audio.model.HYTBaseAudioPlayer

class HYTAudioPlayerFactory {

    companion object{

        fun getAudioPlayer(context: Context): HYTAudioPlayer{
            return HYTBaseAudioPlayer(context);
        }

    }

}