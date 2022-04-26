package org.hyt.hytport.audio.api.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class HYTQueue(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(name = "title") var title: String,
    @ColumnInfo(name = "type") var type: String
);