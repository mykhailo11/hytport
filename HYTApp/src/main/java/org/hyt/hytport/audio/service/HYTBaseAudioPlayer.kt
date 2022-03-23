package org.hyt.hytport.audio.service

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.audiofx.Visualizer
import org.hyt.hytport.audio.api.model.HYTAudioModel
import org.hyt.hytport.audio.api.service.HYTAudioPlayer
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque

class HYTBaseAudioPlayer public constructor(
    context: Context,
    completion: () -> Unit
) : HYTAudioPlayer {

    private val _queue: Deque<HYTAudioModel>;

    private val _player: MediaPlayer;

    private val _context: Context;

    private var _consumer: (ByteArray) -> Unit = {};

    private val _visualizer: Visualizer;

    private inner class HYTDataListener : Visualizer.OnDataCaptureListener {

        override fun onWaveFormDataCapture(visualizer: Visualizer?, buffer: ByteArray?, rating: Int) {
            _consumer.invoke(buffer!!);
        }

        override fun onFftDataCapture(visualizer: Visualizer?, buffer: ByteArray?, rating: Int) {
            return;
        }

    }

    init {
        _queue = ConcurrentLinkedDeque();
        _player = MediaPlayer();
        _context = context;
        _player.setOnCompletionListener {
            completion();
        }
        _player.setOnPreparedListener {
            if (!_player.isPlaying) {
                _player.start();
            }
        }
        _visualizer = Visualizer(_player.audioSessionId);
        _visualizer.captureSize = Visualizer.getCaptureSizeRange()[1];
        _visualizer.setDataCaptureListener(
            HYTDataListener(),
            Visualizer.getMaxCaptureRate() / 2,
            true,
            false
        );
        _visualizer.scalingMode = Visualizer.SCALING_MODE_AS_PLAYED;
    }


    override fun play(): HYTAudioModel {
        if (!_player.isPlaying) {
            _visualizer.enabled = true;
            _player.start();
        }
        return _queue.last;
    }

    override fun play(audio: HYTAudioModel): HYTAudioModel {
        return try {
            val actual: HYTAudioModel = _queue.first {
                it.getId() == audio.getId();
            };
            _queue.remove(actual);
            _queue.offerFirst(actual);
            next();
        } catch (exception: Exception) {
            audio;
        }
    }

    override fun pause(): HYTAudioModel {
        if (_player.isPlaying) {
            _player.pause();
        }
        return _queue.last;
    }

    override fun next(): HYTAudioModel {
        val audio: HYTAudioModel? = _queue.poll();
        _reset(audio);
        return audio!!;
    }

    override fun previous(): HYTAudioModel {
        _queue.offerFirst(_queue.pollLast());
        val audio: HYTAudioModel? = _queue.pollLast();
        _reset(audio);
        return audio!!;
    }

    private fun _reset(audio: HYTAudioModel?): Unit {
        if (audio != null) {
            _player.reset();
            _visualizer.enabled = true;
            _player.setDataSource(_context, audio.getPath()!!);
            _player.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            );
            _player.prepareAsync();
            _queue.offer(audio);
        }
    }

    override fun addNext(next: HYTAudioModel) {
        if (!_queue.any { audio -> audio.getId() == next.getId() }) {
            _queue.offer(next);
        }
    }

    override fun queue(consumer: (Deque<HYTAudioModel>) -> Unit): Unit {
        consumer(_queue);
    }

    override fun isPlaying(): Boolean {
        return _player.isPlaying;
    }

    override fun destroy() {
        _visualizer.enabled = false;
        _visualizer.release();
        if (_player.isPlaying) {
            _player.stop();
        }
        _player.reset();
        _player.release();
    }

    override fun consumer(consumer: (ByteArray) -> Unit) {
        _consumer = consumer;
    }

}