package org.hyt.hytport.audio.service

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import org.hyt.hytport.audio.api.model.HYTAudioModel
import org.hyt.hytport.audio.api.service.HYTAudioPlayer
import java.util.*

class HYTBaseAudioPlayer public constructor(
    queueProvider: ((Deque<HYTAudioModel>) -> Unit) -> Unit,
    context: Context
) : HYTAudioPlayer {

    companion object {

        private val _DEFAULT_AUDITOR: HYTAudioPlayer.Companion.HYTAuditor =
            object : HYTAudioPlayer.Companion.HYTAuditor {};

        private val _DEFAULT_AUDIO_ATTRIBUTES: AudioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .build();

    }

    private val _context: Context;

    private val _queueProvider: ((Deque<HYTAudioModel>) -> Unit) -> Unit;

    private var _auditor: HYTAudioPlayer.Companion.HYTAuditor;

    private val _player: MediaPlayer;

    private var _prepared: Boolean = false;

    private val _progressDelegator: Handler;

    private val _progressWorker: Runnable;

    init {
        _context = context;
        _queueProvider = queueProvider;
        _auditor = _DEFAULT_AUDITOR;
        _player = MediaPlayer();
        _player.setOnCompletionListener {
            _queueCheck { queue: Deque<HYTAudioModel> ->
                _auditor.onComplete(queue.first);
            }
        };
        _player.setOnPreparedListener {
            _prepared = true;
            if (!_player.isPlaying) {
                _player.start();
            }
        };
        _progressDelegator = Handler(Looper.getMainLooper());
        _progressWorker = object : Runnable {

            override fun run() {
                if (_prepared && _player.isPlaying){
                    _auditor.progress(_player.duration, _player.currentPosition);
                }
                _progressDelegator.postDelayed(this, 1000);
            }

        };
        _progressDelegator.postDelayed(_progressWorker, 1000);
    }

    override fun play() {
        _queueCheck { queue: Deque<HYTAudioModel> ->
            val current: HYTAudioModel = queue.first;
            if (!_prepared) {
                _reset(current);
            } else if (!_player.isPlaying) {
                _player.start();
            }
            _auditor.onPlay(current);
        }
    }

    override fun play(audio: HYTAudioModel) {
        _queueProvider { queue: Deque<HYTAudioModel> ->
            val existing: HYTAudioModel? = queue.find {
                it.getId() == audio.getId()
            };
            if (existing != null) {
                queue.remove(existing);
                queue.offerFirst(existing);
                _reset(existing);
                _auditor.onNext(existing);
            } else {
                queue.offerFirst(audio);
                _reset(audio);
                _auditor.onNext(audio);
            }
        }
    }

    override fun isPlaying(consumer: (Boolean) -> Unit) {
        consumer(_prepared && _player.isPlaying);
    }

    override fun current(consumer: (HYTAudioModel) -> Unit) {
        _queueCheck { queue: Deque<HYTAudioModel> ->
            consumer(queue.first);
        }
    }

    override fun queue(consumer: (Deque<HYTAudioModel>) -> Unit) {
        _queueProvider(consumer);
    }

    override fun pause() {
        if (_prepared && _player.isPlaying) {
            _player.pause();
        }
        _queueCheck { queue: Deque<HYTAudioModel> ->
            _auditor.onPause(queue.first);
        }
    }

    override fun next() {
        _queueCheck { queue: Deque<HYTAudioModel> ->
            queue.offer(queue.pop());
            val next: HYTAudioModel = queue.first;
            _reset(next);
            _auditor.onNext(next);
        }
    }

    override fun previous() {
        _queueCheck { queue: Deque<HYTAudioModel> ->
            queue.offerFirst(queue.pollLast());
            val previous: HYTAudioModel = queue.first;
            _reset(previous);
            _auditor.onPrevious(previous);
        }
    }

    private fun _reset(audio: HYTAudioModel): Unit {
        _prepared = false;
        _player.reset();
        _player.setDataSource(
            _context,
            audio.getPath()!!
        );
        _player.setAudioAttributes(_DEFAULT_AUDIO_ATTRIBUTES);
        _player.prepareAsync();
    }

    override fun seek(to: Int) {
        if (_prepared){
            _player.seekTo(to);
        }
    }

    override fun destroy() {
        _auditor.onDestroy();
        resetAuditor();
        _progressDelegator.removeCallbacks(_progressWorker);
        if (_player.isPlaying) {
            _player.stop();
        }
        _player.reset();
        _player.release();
    }

    override fun setAuditor(auditor: HYTAudioPlayer.Companion.HYTAuditor) {
        _auditor = auditor;
        _queueCheck { queue: Deque<HYTAudioModel> ->
            _auditor.onReady(queue.first);
        }
    }

    override fun resetAuditor() {
        _auditor = _DEFAULT_AUDITOR;
    }

    private fun _queueCheck(reaction: (Deque<HYTAudioModel>) -> Unit): Unit {
        _queueProvider { queue: Deque<HYTAudioModel> ->
            if (queue.isNotEmpty()) {
                reaction(queue);
            }
        }
    }

}