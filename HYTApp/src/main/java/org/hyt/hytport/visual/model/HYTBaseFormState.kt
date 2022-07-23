package org.hyt.hytport.visual.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.hyt.hytport.visual.api.model.HYTFormState

class HYTBaseFormState public constructor(
    initialTitle: String,
    initialConfirm: String
): HYTFormState {

    private var _title: String by mutableStateOf(initialTitle);

    private var _confirm: String by mutableStateOf(initialConfirm);

    private var _value: String? = null;

    private val _auditors: MutableList<HYTFormState.Companion.HYTAuditor> = mutableListOf();

    private val _actions: MutableMap<String, (String) -> Unit> = mutableMapOf();

    private var _consumer: ((String) -> Unit)? = null;

    override fun title(): String {
        return _title;
    }

    override fun title(title: String) {
        _title = title;
    }

    override fun confirm(): String {
        return _confirm;
    }

    override fun confirm(confirm: String) {
        _confirm = confirm;
    }

    override fun addAuditor(auditor: HYTFormState.Companion.HYTAuditor) {
        _auditors.add(auditor);
    }

    override fun removeAuditor(auditor: HYTFormState.Companion.HYTAuditor) {
        _auditors.remove(auditor);
    }

    override fun accept() {
        _consumer?.invoke(value());
        _auditors.forEach { auditor: HYTFormState.Companion.HYTAuditor ->
            auditor.onAccept(value());
        }
    }

    override fun consumer(consumer: ((String) -> Unit)?) {
        _consumer = consumer;
    }

    override fun cancel() {
        _auditors.forEach { auditor: HYTFormState.Companion.HYTAuditor ->
            auditor.onCancel(value());
        }
    }

    override fun action(action: String) {
        _actions[action]?.invoke(value());
        _auditors.forEach { auditor: HYTFormState.Companion.HYTAuditor ->
            auditor.onAction(action, value());
        }
    }

    override fun value(): String {
        return _value ?: "";
    }

    override fun value(value: String) {
        _value = value;
    }

    override fun addAction(action: String, consumer: (String) -> Unit) {
        _actions[action] = consumer;
        _auditors.forEach { auditor: HYTFormState.Companion.HYTAuditor ->
            auditor.onActionsChanged();
        }
    }

    override fun removeAction(action: String) {
        if (_actions.containsKey(action)) {
            _actions.remove(action)
            _auditors.forEach { auditor: HYTFormState.Companion.HYTAuditor ->
                auditor.onActionsChanged();
            }
        }
    }

    override fun actions(): Map<String, (String) -> Unit> {
        return _actions;
    }

}