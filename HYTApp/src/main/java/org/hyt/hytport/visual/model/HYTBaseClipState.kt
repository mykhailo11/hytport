package org.hyt.hytport.visual.model

import org.hyt.hytport.visual.api.model.HYTClipState

class HYTBaseClipState: HYTClipState {

    private val _auditors: MutableList<HYTClipState.Companion.HYTAuditor> = ArrayList();

    override fun size(width: Float, height: Float) {
        _auditors.forEach { auditor: HYTClipState.Companion.HYTAuditor ->
            auditor.onSize(width, height);
        }
    }

    override fun expand(expand: Float) {
        _auditors.forEach { auditor: HYTClipState.Companion.HYTAuditor ->
            auditor.onExpand(expand);
        }
    }

    override fun offset(start: Float, top: Float) {
        _auditors.forEach { auditor: HYTClipState.Companion.HYTAuditor ->
            auditor.onOffset(start, top);
        }
    }

    override fun round(round: Float) {
        _auditors.forEach { auditor: HYTClipState.Companion.HYTAuditor ->
            auditor.onRound(round);
        }
    }

    override fun addAuditor(auditor: HYTClipState.Companion.HYTAuditor) {
        _auditors.add(auditor);
    }

    override fun removeAuditor(auditor: HYTClipState.Companion.HYTAuditor) {
        _auditors.remove(auditor);
    }

}