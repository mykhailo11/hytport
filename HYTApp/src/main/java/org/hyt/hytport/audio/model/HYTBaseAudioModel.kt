package org.hyt.hytport.audio.model

import android.net.Uri
import org.hyt.hytport.audio.api.model.HYTAudioModel

class HYTBaseAudioModel: HYTAudioModel{

    private var _id: Long = 0L;

    private var _title: String?;

    private var _artist: String?;

    private var _album: String?;

    private var _albumPath: Uri?;

    private var _path: Uri?;

    private var _duration: Long? = 0L;

    init {
        _title = null;
        _artist = null;
        _album = null;
        _albumPath = null;
        _path = null;
    }

    override fun getId(): Long {
        return _id;
    }

    override fun getTitle(): String? {
        return _title;
    }

    override fun getArtist(): String? {
        return _artist;
    }

    override fun getAlbum(): String? {
        return _album;
    }

    override fun getDuration(): Long? {
        return _duration;
    }

    override fun setId(id: Long): Unit {
        _id = id;
    }

    override fun setTitle(title: String?): Unit {
        _title = title;
    }

    override fun setArtist(artist: String?): Unit {
        _artist = artist;
    }

    override fun setAlbum(album: String?): Unit {
        _album = album;
    }

    override fun setDuration(duration: Long?): Unit {
        _duration = duration;
    }

    override fun getPath(): Uri? {
        return _path;
    }

    override fun setPath(path: Uri?): Unit {
        _path = path;
    }

    override fun getAlbumPath(): Uri? {
        return _albumPath;
    }

    override fun setAlbumPath(album: Uri?) {
        _albumPath = album;
    }

}