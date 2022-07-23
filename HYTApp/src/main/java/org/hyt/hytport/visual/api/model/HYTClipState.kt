package org.hyt.hytport.visual.api.model

interface HYTClipState {

    companion object {

        interface HYTAuditor {

            fun onSize(width: Float, height: Float): Unit {}

            fun onOffset(start: Float, top: Float): Unit {}

            fun onRound(round: Float): Unit {}

            fun onExpand(expand: Float): Unit {}

        }

    }

    fun size(width: Float, height: Float): Unit;
    
    fun offset(start: Float, top: Float): Unit;

    fun expand(expand: Float): Unit;
    
    fun round(round: Float): Unit;

    fun addAuditor(auditor: HYTAuditor): Unit;

    fun removeAuditor(auditor: HYTAuditor): Unit;
    
}