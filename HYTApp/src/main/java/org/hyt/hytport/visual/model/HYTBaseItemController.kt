package org.hyt.hytport.visual.model

import android.graphics.RectF
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.hyt.hytport.audio.api.model.HYTAudioModel
import org.hyt.hytport.visual.api.model.HYTItemController
import java.util.concurrent.ExecutorService

class HYTBaseItemController public constructor(
    executor: ExecutorService
) : HYTItemController {

    private val _auditors: MutableList<HYTItemController.Companion.HYTAuditor>;

    private var _focused: HYTAudioModel? by mutableStateOf(null);

    private var _selected: HYTAudioModel? = null

    private var _offset: RectF = RectF()

    private var _filter: ((HYTAudioModel) -> Boolean)? = null;

    private val _executor: ExecutorService = executor;

    init {
        _auditors = ArrayList();
    }

    override fun filter(): ((HYTAudioModel) -> Boolean)? {
        return _filter;
    }

    override fun filter(filter: ((HYTAudioModel) -> Boolean)?) {
        _executor.submit {
            _filter = filter;
            _auditors.forEach { auditor: HYTItemController.Companion.HYTAuditor ->
                auditor.onFilter(_filter)
            }
        }
    }

    override fun move(start: Float, top: Float, width: Float, height: Float) {
        _offset = RectF(
            start,
            top,
            start + width,
            top + height
        );
        _auditors.forEach { auditor: HYTItemController.Companion.HYTAuditor ->
            auditor.onMove(start, top, width, height);
        }
    }

    override fun offset(): RectF {
        return _offset
    }

    override fun focus(audio: HYTAudioModel?, move: Boolean): Boolean {
        var result: Boolean = true;
        if (move && audio != null) {
            _focused = audio;
        } else if (audio == null) {
            _focused = null;
        }
        _auditors.forEach { auditor: HYTItemController.Companion.HYTAuditor ->
            result = auditor.onFocus(audio, move) && result;
        }
        return result;
    }

    override fun focused(): HYTAudioModel? {
        return _focused;
    }

    override fun select(audio: HYTAudioModel?): Boolean {
        var result: Boolean = true;
        _auditors.forEach { auditor: HYTItemController.Companion.HYTAuditor ->
            result = auditor.onSelect(audio) && result;
        }
        _selected = audio;
        return result;
    }

    override fun selected(): HYTAudioModel? {
        return _selected;
    }

    override fun remove(audio: HYTAudioModel) {
        _auditors.forEach { auditor: HYTItemController.Companion.HYTAuditor ->
            auditor.onRemove(audio);
        }
    }

    override fun addAuditor(auditor: HYTItemController.Companion.HYTAuditor) {
        _auditors.add(auditor);
    }

    override fun removeAuditor(auditor: HYTItemController.Companion.HYTAuditor) {
        _auditors.remove(auditor);
    }

}