package org.hyt.hytport.audio.service

import kotlinx.coroutines.runBlocking
import org.hyt.hytport.audio.access.HYTDatabase
import org.hyt.hytport.audio.api.access.HYTAudioRepository
import org.hyt.hytport.audio.api.access.HYTQueueAccess
import org.hyt.hytport.audio.api.model.*
import org.hyt.hytport.audio.api.service.HYTQueueProvider
import org.hyt.hytport.audio.factory.HYTQueueFactory

class HYTBaseQueueProvider(
    repository: HYTAudioRepository,
    database: HYTDatabase
) : HYTQueueProvider {

    private val _repository: HYTAudioRepository;

    private var _items: List<HYTAudioModel>? = null;

    private val _database: HYTDatabase;

    init {
        _repository = repository;
        _database = database;
    }

    override suspend fun getAll(consumer: (List<String>) -> Unit): Unit {
        val queues: List<HYTQueueOrder> = _database
            .queueAccess()
            .getQueueOrders(_repository.getType());
        consumer(
            queues.map { queue: HYTQueueOrder ->
                queue.queue.title
            }
        );
    }

    override suspend fun getByName(name: String, consumer: (HYTAudioManager) -> Unit): Unit {
        val queueInformation: HYTQueueOrder = _getQueue(name);
        _checkItems { items: List<HYTAudioModel> ->
            val queue: MutableList<HYTAudioModel> =
                queueInformation.orders
                    .mapNotNull { order: HYTOrder ->
                        items.find { audio: HYTAudioModel ->
                            audio.getId().toString() == order.audioId
                        }
                    }
                    .toMutableList()
            consumer(HYTQueueFactory.getManager(queue, queueInformation.queue.title));
        }
    }

    override suspend fun mainstream(consumer: (List<HYTAudioModel>) -> Unit): Unit {
        _checkItems(consumer);
    }

    override suspend fun new(consumer: (List<HYTAudioModel>) -> Unit) {
        val items: List<String> = _database.queueAccess().getAllAudio();
        val new: List<HYTAudioModel>? = _items?.filterNot { audio: HYTAudioModel ->
            items.contains(audio.getId().toString());
        }
        if (new != null) {
            consumer(new);
        }
    }

    override suspend fun save(
        manager: HYTAudioManager,
        saved: ((HYTAudioManager) -> Unit)?
    ) {
        manager.queue { items: List<HYTAudioModel> ->
            runBlocking {
                _save(manager.name(), items);
                saved?.invoke(manager);
            }
        }
    }

    override suspend fun save(
        name: String,
        vararg tracks: HYTAudioModel,
        saved: ((List<HYTAudioModel>) -> Unit)?
    ) {
        val items: List<HYTAudioModel> = tracks.asList();
        _save(name, items);
        saved?.invoke(items);
    }

    override suspend fun add(
        name: String,
        vararg tracks: HYTAudioModel,
        saved: ((List<HYTAudioModel>) -> Unit)?
    ) {
        val access: HYTQueueAccess = _database.queueAccess();
        val queue: HYTQueue = when (
            val checked: HYTQueue? = access.getQueueByTitle(name)
        ) {
            null -> {
                access.addQueue(
                    HYTQueue(
                        title = name,
                        type = _repository.getType()
                    )
                );
                access.getQueueByTitle(name)!!;
            }
            else -> checked
        }
        val existing: List<HYTOrder> = access.getOrdersIn(
            queueId = queue.id,
            items = tracks.map { audio: HYTAudioModel ->
                audio.getId().toString()
            }
        );
        access.prependOrders(
            tracks
                .filterNot { item: HYTAudioModel ->
                    existing.any { order: HYTOrder ->
                        order.audioId == item.getId().toString();
                    }
                }
                .mapIndexed { index: Int, audio: HYTAudioModel ->
                    HYTOrder(
                        audioId = audio.getId().toString(),
                        audioOrder = index.toLong(),
                        queueId = queue.id
                    );
                }
        );
        saved?.invoke(tracks.asList());
    };

    private suspend fun _save(name: String, tracks: List<HYTAudioModel>) {
        val access: HYTQueueAccess = _database.queueAccess();
        val queueInformation: HYTQueueOrder = _getQueue(name);
        val orders: List<HYTOrder> = queueInformation.orders;
        val queue: HYTQueue = queueInformation.queue;
        val matcher: (
            HYTAudioModel,
            HYTOrder
        ) -> Boolean = { audio: HYTAudioModel, order: HYTOrder ->
            audio.getId().toString() == order.audioId
        }
        val ordersSplit: Pair<
                List<HYTOrder>,
                List<HYTOrder>
                > = orders
            .partition { order: HYTOrder ->
                tracks.any { audio: HYTAudioModel ->
                    matcher(audio, order);
                }
            };
        val ordersExisting: List<HYTOrder> = ordersSplit.first;
        val orderPool: Pair<
                List<HYTOrder>,
                List<HYTOrder>
                > = tracks
            .mapIndexed { index: Int,
                          audio: HYTAudioModel ->
                val order: HYTOrder = ordersExisting.find { order: HYTOrder ->
                    matcher(audio, order)
                } ?: HYTOrder(
                    audioId = audio.getId().toString(),
                    queueId = queue.id,
                    audioOrder = index.toLong()
                ).apply {
                    new = true;
                }
                Pair(
                    index,
                    order
                );
            }
            .filter { pair: Pair<Int, HYTOrder> ->
                val order: HYTOrder = pair.second
                pair.first.toLong() != order.audioOrder || order.new
            }
            .map { pair: Pair<Int, HYTOrder> ->
                pair.second.apply {
                    audioOrder = pair.first.toLong()
                }
            }
            .partition { order: HYTOrder ->
                !order.new
            };
        access.removeOrders(ordersSplit.second);
        access.updateOrders(orderPool.first);
        access.addOrders(orderPool.second);
    }

    private suspend fun _getQueue(name: String): HYTQueueOrder {
        val access: HYTQueueAccess = _database.queueAccess();
        return when (
            val checked: HYTQueueOrder? = access.getQueueOrderByTitle(name)
        ) {
            null -> {
                access.addQueue(
                    HYTQueue(
                        title = name,
                        type = _repository.getType()
                    )
                );
                access.getQueueOrderByTitle(name)!!;
            }
            else -> checked
        };
    }

    override suspend fun edit(
        name: String,
        new: String,
        saved: ((Boolean) -> Unit)?
    ) {
        val access: HYTQueueAccess = _database.queueAccess();
        val queue: HYTQueue? = access.getQueueByTitle(name);
        if (queue != null) {
            queue.title = new;
            access.updateQueue(queue);
            saved?.invoke(true);
        } else {
            saved?.invoke(false);
        }
    }

    override suspend fun remove(name: String, saved: (() -> Unit)?) {
        _database.queueAccess().removeQueueByName(name);
        saved?.invoke();
    }

    private fun _checkItems(consumer: (List<HYTAudioModel>) -> Unit): Unit {
        if (_items != null) {
            consumer(_items!!);
        } else {
            _repository.getAllAudio { items: List<HYTAudioModel> ->
                _items = items;
                consumer(_items!!);
            }
        }
    }

}