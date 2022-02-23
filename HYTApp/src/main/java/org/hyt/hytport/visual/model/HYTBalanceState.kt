package org.hyt.hytport.visual.model

import kotlin.math.abs

class HYTBalanceState public constructor(speed: Float): HYTDynamicState(speed) {

    private var _goal: Float = 0.0f;

    private var _state: Float = 0.0f;

    override fun setState(state: Float) {
        _goal = state;
    }

    override fun getState(): Float {
        val difference: Float = _goal - _state;
        if (abs(difference) - _speed > _speed){
            _state += difference * _speed;
        }
        return _state;
    }
}