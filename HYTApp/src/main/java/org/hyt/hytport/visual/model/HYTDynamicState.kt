package org.hyt.hytport.visual.model

import org.hyt.hytport.visual.api.model.HYTState

abstract class HYTDynamicState protected constructor(speed: Float): HYTState{

    protected var _speed: Float;

    init {
        _speed = speed;
    }

}