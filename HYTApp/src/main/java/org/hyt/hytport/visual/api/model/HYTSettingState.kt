package org.hyt.hytport.visual.api.model

interface HYTSettingState {

    companion object {

        interface HYTAuditor {

            fun onToggle(toggle: Boolean): Unit;

        }

    }

    fun title(): String;

    fun title(title: String): Unit;

    fun description(): String;

    fun description(description: String): Unit;

    fun toggle(): Unit;

    fun state(): Boolean;

    fun addAuditor(auditor: HYTAuditor): Unit;

    fun removeAuditor(auditor: HYTAuditor): Unit;

}