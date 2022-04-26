package org.hyt.hytport.audio.api.model

import androidx.room.*

data class HYTQueueOrder(
    @Embedded val queue: HYTQueue,
    @Relation(
        parentColumn = "id",
        entityColumn = "queueId"
    )
    val orders: List<HYTOrder>
);