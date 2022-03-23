package org.hyt.hytport.audio.service

import android.os.Binder
import org.hyt.hytport.audio.api.access.HYTAudioRepository
import org.hyt.hytport.audio.api.model.HYTAudioModel
import org.hyt.hytport.audio.api.service.HYTAudioPlayer
import org.hyt.hytport.audio.api.service.HYTBinder
import java.util.*
import kotlin.collections.ArrayList

class HYTWrapperBinder public constructor(
    player: HYTAudioPlayer
) : Binder(), HYTBinder {

    private val _player: HYTAudioPlayer;

    private val _auditors: MutableList<HYTBinder.Companion.HYTAuditor> = ArrayList();

    private var _repository: HYTAudioRepository?;

    init {
        _player = player;
        _player.consumer { food: ByteArray ->
            _defaultConsumer(food)
        }
        _repository = null;
    }

    override fun play(): HYTAudioModel {
        val audio: HYTAudioModel = _player.play();
        _auditors.forEach {
            it.onPlay(audio);
        }
        return audio;
    }

    override fun play(audio: HYTAudioModel): HYTAudioModel {
        val actualAudio: HYTAudioModel = _player.play(audio);
        _auditors.forEach {
            it.onPlay(actualAudio);
        }
        return actualAudio;
    }

    override fun isPlaying(): Boolean {
        return _player.isPlaying();
    }

    override fun pause(): HYTAudioModel {
        val audio: HYTAudioModel = _player.pause();
        _auditors.forEach {
            it.onPause(audio);
        }
        return audio;
    }

    override fun next(): HYTAudioModel {
        val audio: HYTAudioModel = _player.next();
        _auditors.forEach {
            it.onNext(audio);
        }
        return audio;
    }

    override fun previous(): HYTAudioModel {
        val audio: HYTAudioModel = _player.previous();
        _auditors.forEach {
            it.onPrevious(audio);
        }
        return audio;
    }

    override fun addNext(next: HYTAudioModel) {
        _player.addNext(next);
        _auditors.forEach {
            it.onAddNext(next);
        }
    }

    override fun queue(consumer: (Deque<HYTAudioModel>) -> Unit): Unit {
        _player.queue(consumer);
    }

    override fun destroy() {
        _auditors.forEach {
            it.onDestroy()
        };
        _player.destroy();
    }

    override fun setRepository(repository: HYTAudioRepository) {
        if (_repository == null || _repository!!.javaClass.canonicalName != repository.javaClass.canonicalName) {
            _repository = repository;
            _repository!!.getAllAudio { audios ->
                _player.queue {
                    it.clear();
                }
                audios.forEach { audio ->
                    addNext(audio);
                };
                next();
                _auditors.forEach {
                    it.onRepositoryChanged(_repository!!);
                }
            };
        }
    }

    override fun getRepository(): Class<HYTAudioRepository>? {
        if (_repository == null) {
            return null;
        }
        return _repository!!.javaClass;
    }

    override fun consumer(consumer: (ByteArray) -> Unit) {
        _player.consumer {
            consumer.invoke(it);
            _defaultConsumer(it);
        };
    }

    private fun _defaultConsumer(food: ByteArray): Unit {
        _auditors.forEach {
            it.consumer(food);
        }
    }

    override fun addAuditor(auditor: HYTBinder.Companion.HYTAuditor): Unit {
        _auditors.add(auditor);
        auditor.onReady();
    }

    override fun removeAuditor(auditor: HYTBinder.Companion.HYTAuditor): Unit {
        _auditors.remove(auditor);
    }

}