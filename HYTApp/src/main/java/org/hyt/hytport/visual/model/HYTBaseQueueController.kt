package org.hyt.hytport.visual.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import org.hyt.hytport.visual.api.model.HYTQueueController
import java.util.concurrent.ExecutorService

class HYTBaseQueueController public constructor(
    currentBack: Color,
    hoverBack: Color,
    back: Color,
    text: Color,
    currentText: Color,
    executor: ExecutorService
) : HYTQueueController {

    private val _currentBack: Color = currentBack;

    private val _hoverBack: Color = hoverBack;

    private val _back: Color = back;

    private val _text: Color = text;

    private val _currentText: Color = currentText;

    private val _auditors: MutableList<HYTQueueController.Companion.HYTAuditor> = ArrayList();

    private var _current: String? by mutableStateOf(null)

    private var _hovering: String? by mutableStateOf(null);

    private var _consume: String? by mutableStateOf(null);

    private val _executor: ExecutorService = executor;

    override fun color(queue: String): Color {
        return when (queue) {
            _hovering -> _hoverBack;
            _consume -> _hoverBack;
            _current -> _currentBack;
            else -> _back
        };
    }

    override fun consume(queue: String?) {
        _auditors.forEach { auditor: HYTQueueController.Companion.HYTAuditor ->
            auditor.onConsume(queue);
        }
        _consume = queue;
    }

    override fun consumer(): String? {
        return _consume;
    }

    override fun text(queue: String): Color {
        return when (queue) {
            _current -> _currentText
            else -> _text
        };
    }

    override fun click(queue: String) {
        if (_current == null) {
            _current = queue;
        }
        _auditors.forEach { auditor: HYTQueueController.Companion.HYTAuditor ->
            auditor.onClick(queue);
        }
        _current = queue;
    }

    override fun long(queue: String) {
        _executor.submit {
            _auditors.forEach { auditor: HYTQueueController.Companion.HYTAuditor ->
                auditor.onLong(queue);
            }
        }
    }

    override fun hover(queue: String) {
        _executor.submit {
            _hovering = queue;
            _auditors.forEach { auditor: HYTQueueController.Companion.HYTAuditor ->
                auditor.onHover(queue);
            }
        }
    }

    override fun out(queue: String) {
        _executor.submit {
            _hovering = null;
            _auditors.forEach { auditor: HYTQueueController.Companion.HYTAuditor ->
                auditor.onOut(queue);
            }
        }
    }

    override fun addAuditor(auditor: HYTQueueController.Companion.HYTAuditor) {
        _auditors.add(auditor);
    }

    override fun removeAuditor(auditor: HYTQueueController.Companion.HYTAuditor) {
        _auditors.remove(auditor);
    }

    override fun add(request: ((String) -> Unit)?) {
        _executor.submit {
            _auditors.forEach { auditor: HYTQueueController.Companion.HYTAuditor ->
                auditor.onAdd(request);
            }
        }
    }

    override fun edit(queue: String, save: ((String) -> Unit)?, remove: (() -> Unit)?) {
        _executor.submit {
            _auditors.forEach { auditor: HYTQueueController.Companion.HYTAuditor ->
                auditor.onEdit(
                    queue = queue,
                    save = save,
                    remove = remove
                );
            }
        }
    }

    override fun current(): String? {
        return _current;
    }

    override fun hover(): String? {
        return _hovering;
    }

}