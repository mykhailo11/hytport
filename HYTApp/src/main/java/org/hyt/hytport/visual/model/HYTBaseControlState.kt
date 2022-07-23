package org.hyt.hytport.visual.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.painter.Painter
import org.hyt.hytport.visual.api.model.HYTControlState

class HYTBaseControlState public constructor(
    playing: Boolean = false,
    next: Painter,
    nextActive: Painter,
    previous: Painter,
    previousActive: Painter,
    play: Painter,
    playActive: Painter,
    playPress: Painter
): HYTControlState {

    private var _playing: Boolean by mutableStateOf(playing);

    private val _play: Painter = play;

    private val _playActive: Painter = playActive;

    private val _playPress: Painter = playPress;

    private var _nextHold: Boolean by mutableStateOf(false);

    private val _next: Painter = next;

    private val _nextActive: Painter = nextActive;

    private var _previousHold: Boolean by mutableStateOf(false);

    private var _press: Boolean by mutableStateOf(false);

    private val _previous: Painter = previous;

    private val _previousActive: Painter = previousActive;

    override fun press(press: Boolean) {
        _press = press;
    }

    override fun play() {
        _playing = true;
    }

    override fun pause() {
        _playing = false;
    }

    override fun next(hold: Boolean) {
        _nextHold = hold;
    }

    override fun previous(hold: Boolean) {
        _previousHold = hold;
    }

    override fun playButton(): Painter {
        return when {
            _press -> _playPress;
            _playing -> _playActive;
            else -> _play;
        };
    }

    override fun nextButton(): Painter {
        return when {
            _nextHold -> _nextActive;
            else -> _next;
        };
    }

    override fun previousButton(): Painter {
        return when {
            _previousHold -> _previousActive;
            else -> _previous;
        };
    }

}