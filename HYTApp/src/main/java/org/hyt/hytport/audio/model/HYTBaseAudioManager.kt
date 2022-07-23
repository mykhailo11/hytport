package org.hyt.hytport.audio.model

import org.hyt.hytport.audio.api.model.HYTAudioManager
import org.hyt.hytport.audio.api.model.HYTAudioModel

class HYTBaseAudioManager public constructor(
    queue: MutableList<HYTAudioModel>,
    name: String
): HYTAudioManager {

    private val _name: String;

    private val _queue: MutableList<HYTAudioModel>;

    private val _shuffleQueue: MutableList<HYTAudioModel>;

    private var _shuffle: Boolean = false;

    private var _current: Long = -1L;

    private var _loop: Boolean = false;

    init {
        _queue = queue;
        _shuffleQueue = ArrayList();
        val first: HYTAudioModel? = queue.firstOrNull();
        if (first != null) {
            _current = first.getId();
        }
        _name = name;
    }

    override fun shuffle(shuffle: Boolean) {
        _shuffle = shuffle;
    }

    override fun shuffle(consumer: (Boolean) -> Unit) {
        consumer(_shuffle);
    }

    override fun loop(loop: Boolean) {
        _loop = loop;
    }

    override fun loop(consumer: (Boolean) -> Unit) {
        consumer(_loop);
    }

    override fun next(consumer: (HYTAudioModel) -> Unit) {
        val current: Int = _queue.indexOfFirst { audio: HYTAudioModel ->
            audio.getId() == _current;
        };
        if (_loop) {
            consumer(_queue[current]);
        } else if (!_shuffle) {
            _nextOrder(current, consumer);
        } else {
            _next(current, consumer);
        }
    }

    private fun _next(current: Int, consumer: (HYTAudioModel) -> Unit): Unit {
        if (_shuffleQueue.isEmpty()) {
            _shuffleQueue.addAll(_queue);
        }
        val next: Int = (Math.random() * _shuffleQueue.count()).toInt();
        if (current != next) {
            val audio: HYTAudioModel = _shuffleQueue.removeAt(next);
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

    override fun current(audio: HYTAudioModel?) {
        val id: Long = audio?.getId() ?: -1L;
        _current = id;
    }

    override fun current(consumer: (HYTAudioModel?) -> Unit) {
        val audio: HYTAudioModel? = _queue.find { audio: HYTAudioModel ->
            audio.getId() == _current;
        }
        consumer(audio);
    }

    override fun queue(consumer: (MutableList<HYTAudioModel>) -> Unit) {
        consumer(_queue);
        if (_current == -1L) {
            _queue.firstOrNull()?.also { audio: HYTAudioModel ->
                _current = audio.getId();
            }
        }
    }

    override fun name(): String {
        return _name;
    }
}