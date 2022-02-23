package org.hyt.hytport.audio.model

import android.net.Uri
import org.hyt.hytport.audio.api.model.HYTAudioModel

class HYTBaseAudioModel: HYTAudioModel{

    private var _id: Long = 0L;

    private lateinit var _title: String;

    private lateinit var _artist: String;

    private lateinit var _album: String;

    private var _albumPath: Uri? = null;

    private lateinit var _path: Uri;

    private lateinit var _duration: String;

    override fun getId(): Long {
        return _id;
    }

    override fun getTitle(): String {
        return _title;
    }

    override fun getArtist(): String {
        return _artist;
    }

    override fun getAlbum(): String {
        return _album;
    }

    override fun getDuration(): String {
        return _duration;
    }

    override fun setId(id: Long): Unit {
        _id = id;
    }

    override fun setTitle(title: String): Unit {
        _title = title;
    }

    override fun setArtist(artist: String): Unit {
        _artist = artist;
    }

    override fun setAlbum(album: String): Unit {
        _album = album;
    }

    override fun setDuration(duration: String): Unit {
        _duration = duration;
    }

    override fun getPath(): Uri {
        return _path;
    }

    override fun setPath(path: Uri): Unit {
        _path = path;
    }

    override fun getAlbumPath(): Uri? {
        return _albumPath;
    }

    override fun setAlbumPath(album: Uri) {
        _albumPath = album;
    }
}