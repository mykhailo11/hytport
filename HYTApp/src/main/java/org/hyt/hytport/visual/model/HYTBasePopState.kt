package org.hyt.hytport.visual.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.AnnotatedString
import org.hyt.hytport.visual.api.model.HYTPopState

class HYTBasePopState public constructor(
    initialTitle: String,
    initialContent: AnnotatedString,
    initialConfirm: String,
    initialPainter: Painter?
): HYTPopState {

    private var _title: String by mutableStateOf(initialTitle);

    private var _content: AnnotatedString by mutableStateOf(initialContent);

    private var _painter: Painter? by mutableStateOf(initialPainter);

    private val _auditors: MutableList<HYTPopState.Companion.HYTAuditor> = mutableListOf();

    private var _confirm: String by mutableStateOf(initialConfirm);

    override fun title(): String {
        return _title;
    }

    override fun title(title: String) {
        _title = title;
    }

    override fun content(): AnnotatedString {
        return _content;
    }

    override fun content(content: AnnotatedString) {
        _content = content;
    }

    override fun click() {
        _auditors.forEach { auditor: HYTPopState.Companion.HYTAuditor ->
            auditor.onClick();
        }
    }

    override fun accept() {
        _auditors.forEach { auditor: HYTPopState.Companion.HYTAuditor ->
            auditor.onAccept();
        }
    }

    override fun image(): Painter? {
        return _painter;
    }

    override fun image(painter: Painter?) {
        _painter = painter;
    }

    override fun confirm(): String {
        return _confirm;
    }

    override fun confirm(confirm: String) {
        _confirm = confirm;
    }

    override fun addAuditor(auditor: HYTPopState.Companion.HYTAuditor) {
        _auditors.add(auditor);
    }

    override fun removeAuditor(auditor: HYTPopState.Companion.HYTAuditor) {
        _auditors.remove(auditor);
    }

}