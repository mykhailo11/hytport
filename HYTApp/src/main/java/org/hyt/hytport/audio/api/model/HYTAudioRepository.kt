package org.hyt.hytport.audio.api.model

interface HYTAudioRepository {

    fun getAllAudio(ready: (List<HYTAudioModel>) -> Unit): Unit;

    fun getAudioById(id: Any, ready: (HYTAudioModel) -> Unit): Unit;

    fun getAudioByArtist(artist: String, ready: (List<HYTAudioModel>) -> Unit): Unit;

    fun getAudioByAlbum(album: String, ready: (List<HYTAudioModel>) -> Unit): Unit;

}