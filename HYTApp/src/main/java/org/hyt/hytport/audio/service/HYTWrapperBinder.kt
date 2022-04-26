package org.hyt.hytport.audio.service

import android.media.audiofx.Visualizer
import android.os.Binder
import kotlinx.coroutines.runBlocking
import org.hyt.hytport.audio.api.model.HYTAudioManager
import org.hyt.hytport.audio.api.model.HYTAudioModel
import org.hyt.hytport.audio.api.service.HYTAudioPlayer
import org.hyt.hytport.audio.api.service.HYTBinder
import org.hyt.hytport.audio.api.service.HYTQueueProvider
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

class HYTWrapperBinder(
    provider: HYTQueueProvider
) : Binder(), HYTBinder {

    private var _player: HYTAudioPlayer? = null;

    private val _auditors: MutableList<HYTBinder.Companion.HYTAuditor>;

    private val _auditor: HYTAudioPlayer.Companion.HYTAuditor;

    private val _visualizer: Visualizer;

    private val _executor: ScheduledExecutorService = Executors.newScheduledThreadPool(2);

    private val _provider: HYTQueueProvider;

    init {
        _auditors = ArrayList();
        _auditor = object : HYTAudioPlayer.Companion.HYTAuditor {

            override fun onReady(audio: HYTAudioModel) {
                _executor.submit {
                    _auditors.forEach { auditor: HYTAudioPlayer.Companion.HYTAuditor ->
                        auditor.onReady(audio);
                    }
                }
            }

            override fun onPlay(audio: HYTAudioModel, current: Long) {
                _executor.submit {
                    _auditors.forEach { auditor: HYTAudioPlayer.Companion.HYTAuditor ->
                        auditor.onPlay(audio, current);
                    }
                }
            }

            override fun onPause(audio: HYTAudioModel, current: Long) {
                _executor.submit {
                    _auditors.forEach { auditor: HYTAudioPlayer.Companion.HYTAuditor ->
                        auditor.onPause(audio, current);
                    }
                }
            }

            override fun onNext(audio: HYTAudioModel) {
                _executor.submit {
                    _auditors.forEach { auditor: HYTAudioPlayer.Companion.HYTAuditor ->
                        auditor.onNext(audio);
                    }
                }
            }

            override fun onPrevious(audio: HYTAudioModel) {
                _executor.submit {
                    _auditors.forEach { auditor: HYTAudioPlayer.Companion.HYTAuditor ->
                        auditor.onPrevious(audio);
                    }
                }
            }

            override fun onComplete(audio: HYTAudioModel) {
                if (_auditors.isEmpty() && _player != null) {
                    _player!!.next();
                }
                _auditors.forEach { auditor: HYTAudioPlayer.Companion.HYTAuditor ->
                    auditor.onComplete(audio);
                }
            }

            override fun progress(duration: Int, current: Int) {
                _executor.submit {
                    _auditors.forEach { auditor: HYTAudioPlayer.Companion.HYTAuditor ->
                        auditor.progress(duration, current);
                    }
                }
            }

            override fun onSetManager(manager: HYTAudioManager) {
                _executor.submit {
                    _auditors.forEach { auditor: HYTAudioPlayer.Companion.HYTAuditor ->
                        auditor.onSetManager(manager);
                    }
                }
            }

            override fun onDestroy() {
                _auditors.forEach { auditor: HYTAudioPlayer.Companion.HYTAuditor ->
                    auditor.onDestroy();
                }
            }

        };
        _provider = provider;
        _visualizer = Visualizer(0);
        _visualizer.captureSize = Visualizer.getCaptureSizeRange()[1];
        _visualizer.setDataCaptureListener(
            object : Visualizer.OnDataCaptureListener {

                override fun onWaveFormDataCapture(visualize: Visualizer?, food: ByteArray?, rating: Int) {
                    if (food != null) {
                        _auditors.forEach { auditor: HYTBinder.Companion.HYTAuditor ->
                            auditor.consumer(food);
                        }
                    }
                }

                override fun onFftDataCapture(visualizer: Visualizer?, food: ByteArray?, rating: Int) {}

            },
            Visualizer.getMaxCaptureRate() / 2,
            true,
            false
        );
        _visualizer.scalingMode = Visualizer.SCALING_MODE_AS_PLAYED;
        _visualizer.enabled = true;
    }

    override fun play() {
        _playerCheck { player: HYTAudioPlayer ->
            player.play();
        }
    }

    override fun play(audio: HYTAudioModel) {
        _playerCheck { player: HYTAudioPlayer ->
            player.play(audio);
        }
    }

    override fun isPlaying(consumer: (Boolean) -> Unit) {
        _playerCheck { player: HYTAudioPlayer ->
            player.isPlaying(consumer);
        }
    }

    override fun pause() {
        _playerCheck { player: HYTAudioPlayer ->
            player.pause();
        }
    }

    override fun next() {
        _playerCheck { player: HYTAudioPlayer ->
            player.next();
        }
    }

    override fun previous() {
        _playerCheck { player: HYTAudioPlayer ->
            player.previous();
        }
    }

    override fun seek(to: Int) {
        _playerCheck { player: HYTAudioPlayer ->
            player.seek(to);
        }
    }

    override fun destroy() {
        _executor.shutdown();
        _visualizer.enabled = false;
        _visualizer.release();
        _player = null;
    }

    override fun manager(
        empty: (() -> Unit)?,
        consumer: (HYTAudioManager) -> Unit,
    ) {
        _playerCheck { player: HYTAudioPlayer ->
            player.manager(empty, consumer);
        }
    }

    override fun setManager(manager: HYTAudioManager) {
        _playerCheck { player: HYTAudioPlayer ->
            player.setManager(manager);
        }
    }

    override fun setAuditor(auditor: HYTAudioPlayer.Companion.HYTAuditor) {
        _playerCheck { player: HYTAudioPlayer ->
            player.setAuditor(auditor);
        }
    }

    override fun resetAuditor() {
        _playerCheck { player: HYTAudioPlayer ->
            player.resetAuditor();
        }
    }

    override fun addAuditor(auditor: HYTBinder.Companion.HYTAuditor) {
        _auditors.add(auditor);
        _playerCheck { player: HYTAudioPlayer ->
            player.manager { manager: HYTAudioManager ->
                manager.current { audio: HYTAudioModel ->
                    auditor.onReady(audio);
                }
            }
        }
    }

    override fun removeAuditor(auditor: HYTBinder.Companion.HYTAuditor) {
        _auditors.remove(auditor);
    }

    override fun setPlayer(player: HYTAudioPlayer) {
        _player = player;
        _executor.submit {
            _auditors.forEach { auditor: HYTBinder.Companion.HYTAuditor ->
                auditor.onSetPlayer(player);
            }
        }
        player.setAuditor(_auditor);
    }

    override fun getPlayer(consumer: (player: HYTAudioPlayer?) -> Unit) {
        consumer(_player);
    }

    private fun _playerCheck(reaction: (HYTAudioPlayer) -> Unit): Unit {
        if (_player != null) {
            reaction(_player!!);
        }
    }

    override suspend fun save(name: String) {
        _player?.manager { manager: HYTAudioManager ->
            runBlocking {
                _provider.save(
                    name,
                    manager
                ) { saved: HYTAudioManager ->
                    _auditors.forEach { auditor: HYTBinder.Companion.HYTAuditor ->
                        auditor.onSave(saved);
                    }
                }
            }
        }
    }

}