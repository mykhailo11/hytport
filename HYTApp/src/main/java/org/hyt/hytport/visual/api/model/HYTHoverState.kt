package org.hyt.hytport.visual.api.model

interface HYTHoverState {

    companion object {

        interface HYTAuditor {

            fun onHover(): Unit {}

            fun onOut(): Unit {}

        }

    }

    fun hover(): Unit;

    fun out(): Unit;

    fun addAuditor(auditor: HYTAuditor): Unit;

    fun removeAuditor(auditor: HYTAuditor): Unit;

}