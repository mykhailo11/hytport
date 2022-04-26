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

    @Insert
    suspend fun addQueue(queue: HYTQueue): Unit;

    @Update
    suspend fun updateQueue(queue: HYTQueue): Unit;

    @Delete
    suspend fun removeQueue(queue: HYTQueue): Unit;

    @Insert
    suspend fun addOrder(order: HYTOrder): Unit;

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