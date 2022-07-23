package org.hyt.hytport.audio.api.model

import androidx.room.Embedded
import androidx.room.Relation

data class HYTQueueOrder(
    @Embedded val queue: HYTQueue,
    @Relation(
        parentColumn = "id",
        entityColumn = "queueId"
    )
    val orders: List<HYTOrder>
);