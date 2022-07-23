package org.hyt.hytport.visual.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.hyt.hytport.visual.api.model.HYTEditorState

class HYTBaseEditorState public constructor(
    initialValue: String,
    label: String
): HYTEditorState {

    private val _auditors: MutableList<HYTEditorState.Companion.HYTAuditor> = mutableListOf();

    private var _value: String by mutableStateOf(initialValue);

    private var _label: String by mutableStateOf(label);

    private var _enabled: Boolean by mutableStateOf(true);

    override fun value(): String {
        return _value;
    }

    override fun value(value: String) {
        _value = value;
        _auditors.forEach { auditor: HYTEditorState.Companion.HYTAuditor ->
            auditor.onChange(value);
        }
    }

    override fun label(): String {
        return _label;
    }

    override fun label(label: String) {
        _label = label;
    }

    override fun enabled(): Boolean {
        return _enabled;
    }

    override fun enabled(enabled: Boolean) {
        _enabled = enabled;
    }

    override fun addAuditor(auditor: HYTEditorState.Companion.HYTAuditor) {
        _auditors.add(auditor);
    }

    override fun removeAuditor(auditor: HYTEditorState.Companion.HYTAuditor) {
        _auditors.remove(auditor);
    }

    override fun click() {
        _auditors.forEach { auditor: HYTEditorState.Companion.HYTAuditor ->
            auditor.onLabelClick();
        }
    }

}