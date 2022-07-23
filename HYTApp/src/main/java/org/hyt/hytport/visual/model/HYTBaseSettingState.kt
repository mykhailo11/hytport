package org.hyt.hytport.visual.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.hyt.hytport.visual.api.model.HYTSettingState

class HYTBaseSettingState public constructor(
    initial: Boolean = false,
    initialTitle: String,
    initialDescription: String
) : HYTSettingState {

    private var _title: String by mutableStateOf(initialTitle);

    private var _description: String by mutableStateOf(initialDescription);

    private var _state: Boolean by mutableStateOf(initial);

    private val _auditors: MutableList<HYTSettingState.Companion.HYTAuditor> = ArrayList();

    override fun title(): String {
        return _title;
    }

    override fun title(title: String) {
        _title = title;
    }

    override fun description(): String {
        return _description;
    }

    override fun description(description: String) {
        _description = description;
    }

    override fun toggle() {
        _state = !_state;
        _auditors.forEach { auditor: HYTSettingState.Companion.HYTAuditor ->
            auditor.onToggle(_state);
        }
    }

    override fun state(): Boolean {
        return _state;
    }

    override fun addAuditor(auditor: HYTSettingState.Companion.HYTAuditor) {
        _auditors.add(auditor);
    }

    override fun removeAuditor(auditor: HYTSettingState.Companion.HYTAuditor) {
        _auditors.remove(auditor);
    }

}