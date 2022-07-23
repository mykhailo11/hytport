package org.hyt.hytport.audio.api.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity
data class HYTOrder(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long = 0L,
    @ColumnInfo(name = "audioId") var audioId: String,
    @ColumnInfo(name = "audioOrder") var audioOrder: Long,
    @ColumnInfo(name = "queueId") var queueId: Long,
) {
    @Ignore var new: Boolean = false
}