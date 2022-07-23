package org.hyt.hytport.visual.model

import org.hyt.hytport.visual.api.model.HYTHoverState

class HYTBaseHoverState public constructor(

): HYTHoverState {

    val _auditors: MutableList<HYTHoverState.Companion.HYTAuditor> = ArrayList();

    override fun hover() {
        _auditors.forEach { auditor: HYTHoverState.Companion.HYTAuditor ->
            auditor.onHover();
        }
    }

    override fun out() {
        _auditors.forEach { auditor: HYTHoverState.Companion.HYTAuditor ->
            auditor.onOut();
        }
    }

    override fun addAuditor(auditor: HYTHoverState.Companion.HYTAuditor) {
        _auditors.add(auditor);
    }

    override fun removeAuditor(auditor: HYTHoverState.Companion.HYTAuditor) {
        _auditors.remove(auditor);
    }

}