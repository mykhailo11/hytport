package org.hyt.hytport.audio.access

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import org.hyt.hytport.audio.api.access.HYTQueueAccess
import org.hyt.hytport.audio.api.model.HYTOrder
import org.hyt.hytport.audio.api.model.HYTQueue
import org.hyt.hytport.audio.api.model.HYTQueueOrder

@Database(
    entities = [
        HYTQueue::class,
        HYTOrder::class
    ],
    version = 2
)
abstract class HYTDatabase : RoomDatabase() {

    abstract fun queueAccess(): HYTQueueAccess;

    companion object {

        @Volatile
        private var _INSTANCE: HYTDatabase? = null;
        private val _LOCK = Any()

        operator fun invoke(context: Context) = _INSTANCE ?: synchronized(_LOCK) {
            _INSTANCE ?: Room.databaseBuilder(
                context,
                HYTDatabase::class.java,
                "hyt-database"
            )
                .fallbackToDestructiveMigration()
                .build();
        }

    }

}