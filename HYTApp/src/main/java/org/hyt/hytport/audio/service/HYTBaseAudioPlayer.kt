package org.hyt.hytport.audio.service

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import org.hyt.hytport.audio.api.model.HYTAudioManager
import org.hyt.hytport.audio.api.model.HYTAudioModel
import org.hyt.hytport.audio.api.service.HYTAudioPlayer
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class HYTBaseAudioPlayer public constructor(
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

    private var _manager: HYTAudioManager? = null;

    private var _auditor: HYTAudioPlayer.Companion.HYTAuditor;

    private val _player: MediaPlayer;

    private var _prepared: Boolean = false;

    private val _progressWorker: () -> Unit;

    private val _executor: ScheduledExecutorService;

    init {
        _context = context;
        _auditor = _DEFAULT_AUDITOR;
        _player = MediaPlayer();
        _player.setOnCompletionListener {
            _managerCheck { manager: HYTAudioManager ->
                manager.current { audio: HYTAudioModel ->
                    _auditor.onComplete(audio);
                }
            }
        };
        _player.setOnErrorListener { _, _, _ ->
            true;
        }
        _player.setOnPreparedListener {
            _prepared = true;
            if (!_player.isPlaying) {
                _player.start();
            }
        };
        _executor = Executors.newSingleThreadScheduledExecutor();
        _progressWorker = {
            if (_prepared && _player.isPlaying) {
                _auditor.progress(_player.duration, _player.currentPosition);
            }
        };
        _executor.scheduleAtFixedRate(_progressWorker, 300, 300, TimeUnit.MILLISECONDS);
    }

    override fun play() {
        _managerCheck { manager: HYTAudioManager ->
            manager.current { audio: HYTAudioModel ->
                if (!_prepared) {
                    _reset(audio);
                } else if (!_player.isPlaying) {
                    _player.start();
                }
                _auditor.onPlay(audio, if (_prepared) _player.currentPosition.toLong() else 0L);
            }
        }
    }

    override fun play(audio: HYTAudioModel) {
        _managerCheck { manager: HYTAudioManager ->
            manager.current(audio);
            _reset(audio);
            _auditor.onNext(audio);
        }
    }

    override fun isPlaying(consumer: (Boolean) -> Unit) {
        consumer(_prepared && _player.isPlaying);
    }

    override fun pause() {
        if (_prepared && _player.isPlaying) {
            _player.pause();
        }
        _managerCheck { manager: HYTAudioManager ->
            manager.current { audio: HYTAudioModel ->
                _auditor.onPause(audio, if (_prepared) _player.currentPosition.toLong() else 0L);
            }
        }
    }

    override fun next() {
        _managerCheck { manager: HYTAudioManager ->
            manager.next { audio: HYTAudioModel ->
                _reset(audio);
                _auditor.onNext(audio);
            }
        }
    }

    override fun previous() {
        _managerCheck { manager: HYTAudioManager ->
            manager.previous { audio: HYTAudioModel ->
                _reset(audio);
                _auditor.onPrevious(audio);
            }
        }
    }

    @Synchronized
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
        if (_prepared) {
            _player.seekTo(to);
        }
    }

    override fun destroy() {
        _auditor.onDestroy();
        _executor.shutdown();
        resetAuditor();
        if (_player.isPlaying) {
            _player.stop();
        }
        _player.reset();
        _player.release();
    }

    override fun manger(
        empty: (() -> Unit)?,
        consumer: (HYTAudioManager) -> Unit
    ) {
        if (_manager != null) {
            consumer(_manager!!);
        } else if (empty != null){
            empty();
        }
    }

    override fun setManager(manager: HYTAudioManager) {
        _manager = manager;
        _auditor.onSetManager(_manager!!);
    }

    override fun setAuditor(auditor: HYTAudioPlayer.Companion.HYTAuditor) {
        _auditor = auditor;
        _managerCheck { manager: HYTAudioManager ->
            manager.current { audio: HYTAudioModel ->
                _auditor.onReady(audio);
            }
        }
    }

    override fun resetAuditor() {
        _auditor = _DEFAULT_AUDITOR;
    }

    private fun _managerCheck(
        reaction: (HYTAudioManager) -> Unit
    ): Unit {
        if (_manager != null) {
            reaction(_manager!!);
        }
    }

}