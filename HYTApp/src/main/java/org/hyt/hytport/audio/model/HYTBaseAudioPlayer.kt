package org.hyt.hytport.audio.model

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.audiofx.Visualizer
import org.hyt.hytport.audio.api.model.HYTAudioModel
import org.hyt.hytport.audio.api.model.HYTAudioPlayer
import org.hyt.hytport.audio.factory.HYTAudioFactory
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque
import kotlin.collections.ArrayList

class HYTBaseAudioPlayer public constructor(
    context: Context
) : HYTAudioPlayer {

    private val _queue: Deque<HYTAudioModel>;

    private val _audits: MutableList<HYTAudioPlayer.HYTAudioPlayerAudit> = ArrayList();

    private val _player: MediaPlayer;

    private val _context: Context;

    private val _visualizer: Visualizer;

    private var _ready: Boolean = false;

    private inner class HYTDataListener : Visualizer.OnDataCaptureListener {

        override fun onWaveFormDataCapture(visualizer: Visualizer?, buffer: ByteArray?, rating: Int) {
            _audits.forEach {
                it.consumer(buffer!!);
            }
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
            next();
        }
        _player.setOnPreparedListener {
            _ready = true;
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
            _audits.forEach {
                it.onPlay(_queue.last);
            }
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
            //_visualizer.enabled = false;
            _player.pause();
            _audits.forEach {
                it.onPause(_queue.last);
            }
        }
        return _queue.last;
    }

    override fun next(): HYTAudioModel {
        val audio: HYTAudioModel? = _queue.poll();
        _reset(audio);
        if (audio != null) {
            _audits.forEach {
                it.onNext(audio);
            }
        }
        return audio!!;
    }

    override fun previous(): HYTAudioModel {
        _queue.offerFirst(_queue.pollLast());
        val audio: HYTAudioModel? = _queue.pollLast();
        _reset(audio);
        if (audio != null) {
            _audits.forEach {
                it.onPrevious(audio);
            }
        }
        return audio!!;
    }

    private fun _reset(audio: HYTAudioModel?): Unit {
        if (audio != null) {
            _player.reset();
            _ready = false;
            _visualizer.enabled = true;
            _player.setDataSource(_context, audio.getPath());
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
            _audits.forEach {
                it.onAddNext(next);
            }
        }
    }

    override fun queue(): Deque<HYTAudioModel> {
        return _queue;
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
        if (_audits.isNotEmpty()){
            _audits.forEach {
                if (_queue.isEmpty()){
                    it.onDestroy(HYTAudioFactory.getAudioModel());
                }else{
                    it.onDestroy(_queue.last);
                }
            }
        }
    }

    override fun addAudit(audit: HYTAudioPlayer.HYTAudioPlayerAudit): Int {
        _audits.add(audit);
        if (_player.isPlaying) {
            audit.onReady();
            audit.onPlay(_queue.last);
        } else if (_ready) {
            audit.onReady();
            audit.onPause(_queue.last);
        }
        audit.setId(_audits.indexOf(audit));
        return audit.getId();
    }

    override fun removeAudit(audit: Int) {
        _audits.removeIf {
            it.getId() == audit;
        }
    }

}