package org.hyt.hytport.audio.model

import org.hyt.hytport.audio.api.model.HYTAudioManager
import org.hyt.hytport.audio.api.model.HYTAudioModel

class HYTBaseAudioManager public constructor(
    queue: MutableList<HYTAudioModel>
): HYTAudioManager {

    private val _queue: MutableList<HYTAudioModel>;

    private var _shuffle: Boolean = false;

    private var _current: Long = -1L;

    init {
        _queue = queue;
        val first: HYTAudioModel? = queue.firstOrNull();
        if (first != null) {
            _current = first.getId();
        }
    }

    override fun shuffle(shuffle: Boolean) {
        _shuffle = shuffle;
    }

    override fun shuffle(consumer: (Boolean) -> Unit) {
        consumer(_shuffle);
    }

    override fun next(consumer: (HYTAudioModel) -> Unit) {
        val current: Int = _queue.indexOfFirst { audio: HYTAudioModel ->
            audio.getId() == _current;
        };
        if (!_shuffle) {
            _nextOrder(current, consumer);
        } else {
            _next(current, consumer);
        }
    }

    private fun _next(current: Int, consumer: (HYTAudioModel) -> Unit): Unit {
        val next: Int = (Math.random() * _queue.count()).toInt();
        if (current != next) {
            val audio: HYTAudioModel = _queue[next];
            _current = audio.getId();
            consumer(audio);
        } else {
            _nextOrder(current, consumer);
        }
    }

    private fun _nextOrder(current: Int, consumer: (HYTAudioModel) -> Unit): Unit {
        if (current != -1) {
            val audio: HYTAudioModel = _queue[(current + 1).mod(_queue.count())];
            _current = audio.getId();
            consumer(audio);
        } else if (_queue.isNotEmpty()){
            val first: HYTAudioModel = _queue.first();
            _current = first.getId();
            consumer(first);
        }
    }

    override fun previous(consumer: (HYTAudioModel) -> Unit) {
        val current: Int = _queue.indexOfFirst { audio: HYTAudioModel ->
            audio.getId() == _current;
        };
        if (current != -1) {
            val audio: HYTAudioModel = _queue[(current - 1).mod(_queue.count())];
            _current = audio.getId();
            consumer(audio);
        } else if (_queue.isNotEmpty()){
            val first: HYTAudioModel = _queue.first();
            _current = first.getId();
            consumer(first);
        }
    }

    override fun current(audio: HYTAudioModel) {
        val id: Long = audio.getId();
        val existing: HYTAudioModel? = _queue.find { item: HYTAudioModel ->
            item.getId() == id;
        };
        if (existing == null) {
            _queue.add(0, audio);
        }
        _current = id;
    }

    override fun current(consumer: (HYTAudioModel) -> Unit) {
        val audio: HYTAudioModel? = _queue.find { audio: HYTAudioModel ->
            audio.getId() == _current;
        }
        if (audio != null) {
            consumer(audio);
        }
    }

    override fun queue(consumer: (MutableList<HYTAudioModel>) -> Unit) {
        consumer(_queue);
    }

}