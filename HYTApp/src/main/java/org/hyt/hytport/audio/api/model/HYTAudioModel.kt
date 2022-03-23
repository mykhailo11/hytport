package org.hyt.hytport.audio.api.model

import android.net.Uri

interface HYTAudioModel {

    fun getId(): Long?;

    fun getTitle(): String?;

    fun getArtist(): String?;

    fun getAlbum(): String?;

    fun getAlbumPath(): Uri?;

    fun getDuration(): Long?;

    fun setId(id: Long?): Unit;

    fun setTitle(title: String?): Unit;

    fun setArtist(artist: String?): Unit;

    fun setAlbum(album: String?): Unit;

    fun setAlbumPath(album: Uri?): Unit;

    fun setDuration(duration: Long?): Unit;

    fun getPath(): Uri?;

    fun setPath(path: Uri?): Unit;

}