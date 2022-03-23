package org.hyt.hytport.audio.api.access

import org.hyt.hytport.audio.api.model.HYTAudioModel

interface HYTAudioRepository {

    fun getAllAudio(ready: (List<HYTAudioModel>) -> Unit): Unit;

    fun getAudioById(id: Any, ready: (HYTAudioModel?) -> Unit): Unit;

}