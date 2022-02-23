package org.hyt.hytport.visual.factory

import org.hyt.hytport.visual.api.model.HYTState
import org.hyt.hytport.visual.model.HYTAbsorbState
import org.hyt.hytport.visual.model.HYTBalanceState
import org.hyt.hytport.visual.model.HYTPulseState

class HYTStateFactory {

    companion object{

        fun getPulseState(speed: Float): HYTState {
            return HYTPulseState(speed);
        }

        fun getBalanceState(speed: Float): HYTState {
            return HYTBalanceState(speed);
        }

        fun getAbsorbState(speed: Float): HYTState {
            return HYTAbsorbState(speed);
        }

    }

}