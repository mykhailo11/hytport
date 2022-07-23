package org.hyt.hytport.visual.model

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.hyt.hytport.visual.api.model.HYTPlayerScrollState

class HYTBasePlayerScrollState public constructor(
    slider: Float = 0.0f,
    max: Float = 10.0f,
    artistScroll: ScrollState,
    titleScroll: ScrollState
): HYTPlayerScrollState {

    private var _slider: Float by mutableStateOf(slider);

    private var _sliding: Boolean by mutableStateOf(false);

    private var _max: Float by mutableStateOf(max);

    private val _artistScroll: ScrollState = artistScroll;

    private val _titleScroll: ScrollState = titleScroll;

    override fun slider(): Float {
        return _slider;
    }

    override fun slider(slide: Float) {
        _slider = slide;
    }

    override fun sliding(): Boolean {
        return _sliding;
    }

    override fun sliding(sliding: Boolean) {
        _sliding = sliding;
    }

    override fun max(): Float {
        return if (_max > _slider) {
            _max;
        } else {
            _slider;
        }
    }

    override fun max(max: Float) {
        _max = max;
    }

    override fun artistScroll(): ScrollState {
        return _artistScroll;
    }

    override fun titleScroll(): ScrollState {
        return _titleScroll;
    }

}