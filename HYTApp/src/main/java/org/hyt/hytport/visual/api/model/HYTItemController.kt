package org.hyt.hytport.visual.api.model

import android.graphics.RectF
import org.hyt.hytport.audio.api.model.HYTAudioModel

interface HYTItemController {

    companion object {

        interface HYTAuditor {

            fun onRemove(audio: HYTAudioModel): Unit{}

            fun onMove(start: Float, top: Float, width: Float, height: Float): Unit{}

            fun onFocus(audio: HYTAudioModel?, move: Boolean): Boolean {
                return true;
            }

            fun onSelect(audio: HYTAudioModel?): Boolean {
                return true;
            }

            fun onFilter(filter: ((HYTAudioModel) -> Boolean)?): Unit{}

        }

    }

    fun filter(): ((HYTAudioModel) -> Boolean)?

    fun filter(filter: ((HYTAudioModel) -> Boolean)?): Unit;

    fun move(start: Float, top: Float, width: Float, height: Float): Unit;

    fun offset(): RectF;

    fun focus(audio: HYTAudioModel?, move: Boolean): Boolean;

    fun focused(): HYTAudioModel?;

    fun select(audio: HYTAudioModel?): Boolean;

    fun selected(): HYTAudioModel?;

    fun remove(audio: HYTAudioModel): Unit;

    fun addAuditor(auditor: HYTAuditor): Unit;

    fun removeAuditor(auditor: HYTAuditor): Unit;

}