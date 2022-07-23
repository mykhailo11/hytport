package org.hyt.hytport.visual.api.model

interface HYTFormState {

    companion object {

        interface HYTAuditor {

            fun onAccept(value: String): Unit{}

            fun onCancel(value: String): Unit{}

            fun onAction(action: String, value: String): Unit{};

            fun onActionsChanged(): Unit{}

        }

    }

    fun title(): String;

    fun title(title: String): Unit;

    fun confirm(): String;

    fun confirm(confirm: String): Unit;

    fun addAuditor(auditor: HYTAuditor): Unit;

    fun removeAuditor(auditor: HYTAuditor): Unit;

    fun accept(): Unit;

    fun consumer(consumer: ((String) -> Unit)?): Unit;

    fun cancel(): Unit;

    fun action(action: String);

    fun addAction(action: String, consumer: (String) -> Unit): Unit;

    fun removeAction(action: String): Unit;

    fun actions(): Map<String, (String) -> Unit>;

    fun value(): String;

    fun value(value: String): Unit;

}