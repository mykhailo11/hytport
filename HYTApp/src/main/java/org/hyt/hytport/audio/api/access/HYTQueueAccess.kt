package org.hyt.hytport.audio.api.access

import androidx.room.*
import org.hyt.hytport.audio.api.model.HYTOrder
import org.hyt.hytport.audio.api.model.HYTQueue
import org.hyt.hytport.audio.api.model.HYTQueueOrder

@Dao
interface HYTQueueAccess {

    @Transaction
    suspend fun getQueueOrders(type: String): List<HYTQueueOrder> {
        return getQueuesByType(type).map { queue: HYTQueue ->
            HYTQueueOrder(
                queue,
                getOrdersByQueue(queue.id)
            )
        };
    }

    @Transaction
    suspend fun getQueueOrderById(id: Long): HYTQueueOrder? {
        return HYTQueueOrder(
            getQueueById(id),
            getOrdersByQueue(id)
        );
    }

    @Transaction
    suspend fun getQueueOrderByTitle(title: String): HYTQueueOrder? {
        val queue: HYTQueue = getQueueByTitle(title) ?: return null;
        return HYTQueueOrder(
            queue,
            getOrdersByQueue(queue.id)
        );

    }

    @Query("SELECT * FROM HYTQueue WHERE type = :type")
    suspend fun getQueuesByType(type: String): List<HYTQueue>;

    @Query("SELECT * FROM HYTQueue WHERE id == :id")
    suspend fun getQueueById(id: Long): HYTQueue;

    @Query ("SELECT * FROM HYTQueue WHERE title == :title LIMIT 1")
    suspend fun getQueueByTitle(title: String): HYTQueue?;

    @Query("SELECT * FROM HYTOrder WHERE queueId == :id ORDER BY audioOrder ASC")
    suspend fun getOrdersByQueue(id: Long): List<HYTOrder>;

    @Query("SELECT * FROM HYTOrder WHERE queueId == :queueId and audioId == :audioId LIMIT 1")
    suspend fun getOrderByAudioQueue(audioId: String, queueId: Long): HYTOrder?

    @Query("SELECT * FROM HYTOrder WHERE audioId == :audioId and queueId == (SELECT id FROM HYTQueue WHERE title == :title LIMIT 1)")
    suspend fun getOrderByAudioQueueTitle(audioId: String, title: String): HYTOrder?

    @Query("SELECT DISTINCT audioId FROM HYTOrder")
    suspend fun getAllAudio(): List<String>;

    @Transaction
    suspend fun removeQueueWithOrders(name: String): Unit {
        val queue: HYTQueue? = getQueueByTitle(name);
        if (queue != null) {
            removeQueueOrders(queue.id);
            removeQueueByName(queue.title);
        }
    }

    @Query("DELETE FROM HYTQueue WHERE title = :name")
    suspend fun removeQueueByName(name: String): Unit;

    @Query("DELETE FROM HYTOrder WHERE queueId = :queueId")
    suspend fun removeQueueOrders(queueId: Long): Unit;

    @Insert
    suspend fun addQueue(queue: HYTQueue): Long;

    @Update
    suspend fun updateQueue(queue: HYTQueue): Unit;

    @Delete
    suspend fun removeQueue(queue: HYTQueue): Unit;

    @Insert
    suspend fun addOrder(order: HYTOrder): Unit;

    @Transaction
    suspend fun prependOrders(orders: List<HYTOrder>): Unit {
        pushOrders(orders.count());
        addOrders(orders);
    }

    @Query("SELECT * FROM HYTOrder WHERE queueId = :queueId AND audioId IN (:items)")
    suspend fun getOrdersIn(queueId: Long, items: List<String>): List<HYTOrder>;

    @Query("UPDATE HYTOrder SET audioOrder = audioOrder + :step")
    suspend fun pushOrders(step: Int): Unit;

    @Transaction
    @Insert
    suspend fun addOrders(orders: List<HYTOrder>): Unit;

    @Update
    suspend fun updateOrder(order: HYTOrder): Unit;

    @Transaction
    @Update
    suspend fun updateOrders(orders: List<HYTOrder>): Unit;

    @Delete
    suspend fun removeOrder(order: HYTOrder): Unit;

    @Transaction
    @Delete
    suspend fun removeOrders(orders: List<HYTOrder>): Unit;

}