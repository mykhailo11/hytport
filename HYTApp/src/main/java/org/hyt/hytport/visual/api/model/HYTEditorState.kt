package org.hyt.hytport.visual.api.model

interface HYTEditorState {

    companion object {

        interface HYTAuditor {

            fun onChange(value: String): Unit{}

            fun onLabelClick(): Unit{}

        }

    }

    fun value(): String;

    fun enabled(): Boolean;

    fun enabled(enabled: Boolean): Unit;

    fun value(value: String): Unit;

    fun label(): String;

    fun label(label: String): Unit;

    fun click(): Unit;

    fun addAuditor(auditor: HYTAuditor): Unit;

    fun removeAuditor(auditor: HYTAuditor): Unit;

}