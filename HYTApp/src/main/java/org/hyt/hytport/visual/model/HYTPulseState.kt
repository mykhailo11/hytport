package org.hyt.hytport.visual.model

import kotlin.math.abs

class HYTPulseState public constructor(speed: Float): HYTDynamicState(speed) {

    private var _goal: Float = 0.0f;

    private var _state: Float = 0.0f;

    override fun setState(state: Float) {
        val absoluteState: Float = abs(state);
        if (absoluteState >= _goal){
            _goal = absoluteState;
        }
    }

    override fun getState(): Float {
        if (_goal > 0.0f && _state < _goal){
            _state += 2.0f * _speed;
        }else{
            _goal = -_speed;
        }
        if (_state > _speed && _goal <= 0.0f){
            _state -= _speed;
        }
        return _state;
    }
}