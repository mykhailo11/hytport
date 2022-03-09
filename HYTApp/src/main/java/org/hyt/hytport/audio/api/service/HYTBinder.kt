package org.hyt.hytport.audio.api.service

import android.os.IBinder
import org.hyt.hytport.audio.api.model.HYTAudioPlayer
import org.hyt.hytport.audio.api.model.HYTAudioRepository

interface HYTBinder: HYTAudioPlayer, IBinder {

    fun setRepository(repository: HYTAudioRepository): Unit;

    fun getRepository(): String?;

}