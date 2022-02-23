package org.hyt.hytport.visual.model

import kotlin.math.abs

class HYTAbsorbState public constructor(speed: Float): HYTDynamicState(speed) {

    private var _goal = 0.0f;

    private var _state = 0.0f;

    override fun setState(state: Float) {
        _goal += abs(state);
    }

    override fun getState(): Float {
        if (_state < _goal){
            _state += _speed * (_goal - _state);
        }
        return _state;
    }
}