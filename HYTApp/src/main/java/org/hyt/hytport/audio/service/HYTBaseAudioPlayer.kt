package org.hyt.hytport.audio.service

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
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

    private val _audio: AudioManager;

    private val _request: AudioFocusRequest;

    private var _manager: HYTAudioManager? = null;

    private var _auditor: HYTAudioPlayer.Companion.HYTAuditor;

    private val _player: MediaPlayer;

    private var _current: HYTAudioModel? = null;

    private var _respect: Boolean = false;

    private var _prepared: Boolean = false;

    private val _progressWorker: () -> Unit;

    private val _executor: ScheduledExecutorService;

    init {
        _context = context;
        _audio = context.getSystemService(AudioManager::class.java);
        _player = MediaPlayer();
        _auditor = _DEFAULT_AUDITOR;
        _request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(_DEFAULT_AUDIO_ATTRIBUTES)
            .setAcceptsDelayedFocusGain(true)
            .setOnAudioFocusChangeListener { focus: Int ->
                when {
                    focus == AudioManager.AUDIOFOCUS_GAIN && !_player.isPlaying -> play();
                    else -> pause();
                }
            }
            .build();
        _player.setOnCompletionListener {
            if (_current != null) {
                _auditor.onComplete(_current!!);
            }
        };
        _player.setOnErrorListener { _, _, _ ->
            true;
        }
        _player.setOnPreparedListener {
            _prepared = true;
            play();
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
        if (!_prepared && _current != null) {
            _reset(_current!!);
        } else if (!_player.isPlaying && _respect) {
            _focus();
        } else if (!_player.isPlaying) {
            _player.start();
            _current?.let { item: HYTAudioModel ->
                current { current: Long ->
                    _auditor.onPlay(item, current);
                }
            }
        }
    }

    override fun respectFocus(respect: Boolean) {
        _audio.abandonAudioFocusRequest(_request);
        _respect = respect;
    }

    private fun _focus(): Unit {
        _audio.abandonAudioFocusRequest(_request);
        if (
            _respect
            && _audio.requestAudioFocus(_request) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            && !_player.isPlaying
        ) {
            _player.start();
            if (_current != null) {
                current { current: Long ->
                    _auditor.onPlay(_current!!, current);
                }
            }
        }
    }

    override fun current(consumer: (Long) -> Unit) {
        consumer(if (_prepared) _player.currentPosition.toLong() else 0L);
    }

    override fun play(audio: HYTAudioModel) {
        _managerCheck { manager: HYTAudioManager ->
            manager.current(audio);
            _current = audio;
            _reset(audio);
            _auditor.onNext(audio);
        }
    }

    override fun isPlaying(consumer: (Boolean) -> Unit) {
        consumer(_prepared && _player.isPlaying);
    }

    override fun pause() {
        _audio.abandonAudioFocusRequest(_request);
        if (_prepared && _player.isPlaying) {
            _player.pause();
        }
        if (_current != null) {
            current { current: Long ->
                _auditor.onPause(_current!!, current);
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
        _current = audio;
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
            _current?.let { audio: HYTAudioModel ->
                _auditor.onSeek(audio, audio.getDuration()?.toInt() ?: 0, to)
            }
        }
    }

    override fun destroy() {
        _audio.abandonAudioFocusRequest(_request);
        _auditor.onDestroy();
        _executor.shutdown();
        resetAuditor();
        if (_player.isPlaying) {
            _player.stop();
        }
        _player.reset();
        _player.release();
    }

    override fun manager(
        empty: (() -> Unit)?,
        consumer: (HYTAudioManager) -> Unit
    ) {
        if (_manager != null) {
            consumer(_manager!!);
        } else if (empty != null) {
            empty();
        }
    }

    override fun setManager(manager: HYTAudioManager) {
        _manager = manager;
        if (!_player.isPlaying && _current == null) {
            _manager!!.current { audio: HYTAudioModel? ->
                _current = audio;
                _auditor.onSetManager(_manager!!, _current);
            }
        } else {
            manager.queue { queue: MutableList<HYTAudioModel> ->
                val audio: HYTAudioModel? = queue.firstOrNull { audio: HYTAudioModel ->
                    audio.getId() == _current?.getId();
                }
                manager.current(audio);
            }
            _auditor.onSetManager(_manager!!, _current);
        }
    }

    override fun setAuditor(auditor: HYTAudioPlayer.Companion.HYTAuditor) {
        _auditor = auditor;
        current { current: Long ->
            _auditor.onReady(_current, current);
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