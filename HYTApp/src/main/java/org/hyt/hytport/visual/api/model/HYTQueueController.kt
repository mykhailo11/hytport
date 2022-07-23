package org.hyt.hytport.visual.api.model

import androidx.compose.ui.graphics.Color

interface HYTQueueController {

    companion object {

        interface HYTAuditor {

            fun onClick(queue: String): Unit {}

            fun onLong(queue: String): Unit {}

            fun onHover(queue: String): Unit {}

            fun onOut(queue: String) {}

            fun onAdd(request: ((String) -> Unit)?): Unit {}

            fun onConsume(queue: String?): Unit {}

            fun onEdit(
                queue: String,
                save: ((String) -> Unit)?,
                remove: (() -> Unit)?
            ): Unit {}

            fun onRemove(queue: String): Unit {}

        }

    }

    fun consume(queue: String?): Unit;

    fun consumer(): String?;

    fun color(queue: String): Color;

    fun text(queue: String): Color;

    fun click(queue: String): Unit;

    fun long(queue: String): Unit;

    fun hover(queue: String): Unit;

    fun out(queue: String): Unit;

    fun add(request: ((String) -> Unit)? = null): Unit;

    fun edit(
        queue: String,
        save: ((String) -> Unit)? = null,
        remove: (() -> Unit)? = null
    );

    fun current(): String?;

    fun hover(): String?;

    fun addAuditor(auditor: HYTAuditor): Unit;

    fun removeAuditor(auditor: HYTAuditor): Unit;

}