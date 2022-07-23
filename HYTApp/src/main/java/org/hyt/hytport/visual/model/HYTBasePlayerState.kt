package org.hyt.hytport.visual.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.hyt.hytport.visual.api.model.HYTPlayerState

class HYTBasePlayerState public constructor(
    artistDefault: String = "Artist",
    titleDefault: String = "Title",
    show: Boolean = false
): HYTPlayerState {

    private var _artist: String by mutableStateOf(artistDefault);

    private var _title: String by mutableStateOf(titleDefault);

    private var _shuffle: Boolean by mutableStateOf(false);

    private var _loop: Boolean by mutableStateOf(false);

    private var _show: Boolean by mutableStateOf(show);

    private var _lock: Boolean by mutableStateOf(false);

    override fun artist(): String {
        return _artist;
    }

    override fun artist(artist: String) {
        _artist = artist;
    }

    override fun title(): String {
        return _title;
    }

    override fun title(title: String) {
        _title = title;
    }

    override fun shuffle(): Boolean {
        return _shuffle;
    }

    override fun shuffle(shuffle: Boolean) {
        _shuffle = shuffle;
    }

    override fun loop(): Boolean {
        return _loop;
    }

    override fun loop(loop: Boolean) {
        _loop = loop;
    }

    override fun show(): Boolean {
        return _lock || _show;
    }

    override fun lock(lock: Boolean) {
        _lock = lock;
    }

    override fun show(show: Boolean) {
        _show = show;
    }

}