package org.hyt.hytport.audio.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import androidx.media.app.NotificationCompat.DecoratedMediaCustomViewStyle
import androidx.media.session.MediaButtonReceiver
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.hyt.hytport.R
import org.hyt.hytport.audio.access.HYTDatabase
import org.hyt.hytport.audio.api.model.HYTAudioManager
import org.hyt.hytport.audio.api.model.HYTAudioModel
import org.hyt.hytport.audio.api.service.HYTAudioPlayer
import org.hyt.hytport.audio.api.service.HYTBinder
import org.hyt.hytport.audio.api.service.HYTQueueProvider
import org.hyt.hytport.audio.factory.HYTAudioFactory
import org.hyt.hytport.audio.factory.HYTAudioPlayerFactory
import org.hyt.hytport.audio.factory.HYTQueueFactory
import org.hyt.hytport.audio.util.HYTAudioUtil
import org.hyt.hytport.util.HYTUtil
import org.hyt.hytport.visual.service.HYTApp

class HYTService : LifecycleService() {

    private lateinit var _binder: HYTBinder;

    private var _player: HYTAudioPlayer? = null;

    private var _playIntent: String? = null;

    private var _nextIntent: String? = null;

    private var _previousIntent: String? = null;

    private var _destroyIntent: String? = null;

    private var _mainstream: String? = null;

    private var _preferences: SharedPreferences? = null;

    private var _preferencesAuditor: SharedPreferences.OnSharedPreferenceChangeListener? = null;

    private var _queueProvider: HYTQueueProvider? = null;

    private lateinit var _mediaSessionEditor: (
            (
            MediaSessionCompat,
            PlaybackStateCompat.Builder,
            MediaMetadataCompat.Builder
        ) -> Unit
    ) -> Unit;

    private lateinit var _notificationEditor: (
            (
            NotificationCompat.Builder,
            NotificationManager
        ) -> Unit
    ) -> Unit;

    private lateinit var _auditor: HYTBinder.Companion.HYTAuditor;

    override fun onCreate() {
        super.onCreate();
        _preferences = getSharedPreferences(
            resources.getString(R.string.preferences),
            Context.MODE_PRIVATE
        );
        val respect: String = resources.getString(R.string.settings_audio_focus);
        _queueProvider = HYTQueueFactory.getQueueProvider(
            HYTAudioFactory.getAudioRepository(contentResolver),
            HYTDatabase(this)
        );
        _binder = HYTAudioFactory.getBinder(_queueProvider!!);
        _preferencesAuditor = SharedPreferences.OnSharedPreferenceChangeListener { preferences: SharedPreferences,
                                                                                   key: String ->
            if (key == respect) {
                _binder.respectFocus(preferences.getBoolean(respect, true));
            }
        };
        _preferences?.registerOnSharedPreferenceChangeListener(_preferencesAuditor);
        _playIntent = resources.getString(R.string.hyt_service_play);
        _nextIntent = resources.getString(R.string.hyt_service_next);
        _previousIntent = resources.getString(R.string.hyt_service_previous);
        _destroyIntent = resources.getString(R.string.hyt_service_destroy);
        _mediaSessionEditor = HYTAudioUtil.mediaSession(
            javaClass.canonicalName!!,
            this,
            _binder
        );

        val id: String = resources.getString(R.string.hyt_channel);
        _notificationEditor = HYTAudioUtil.notification(
            this,
            id,
            R.drawable.hyt_player_icon_200dp
        );
        _auditor = object : HYTBinder.Companion.HYTAuditor {

            override fun onReady(audio: HYTAudioModel?, current: Long) {
                if (audio != null) {
                    _reset(audio);
                }
            }

            override fun onNext(audio: HYTAudioModel) {
                _reset(audio);
                _setPlayback(
                    duration = audio.getDuration()?.toInt() ?: 0,
                    current = 0,
                    state = PlaybackStateCompat.STATE_PLAYING
                );
                _setNotification(audio);
            }

            override fun onPrevious(audio: HYTAudioModel) {
                _reset(audio);
                _setPlayback(
                    duration = audio.getDuration()?.toInt() ?: 0,
                    current = 0,
                    state = PlaybackStateCompat.STATE_PLAYING
                );
                _setNotification(audio);
            }

            override fun onComplete(audio: HYTAudioModel) {
                _player!!.next();
            }

            override fun onSetManager(manager: HYTAudioManager, audio: HYTAudioModel?) {
                if (audio != null) {
                    _reset(audio);
                }
            }

            private fun _reset(audio: HYTAudioModel): Unit {
                _setMetadata(audio);
                _setNotification(audio);
            }

            override fun onPlay(audio: HYTAudioModel, current: Long) {
                _setPlayback(
                    duration = audio.getDuration()?.toInt() ?: 0,
                    current = current.toInt(),
                    state = PlaybackStateCompat.STATE_PLAYING
                );
                _setNotification(audio);
            }

            override fun onPause(audio: HYTAudioModel, current: Long) {
                _setPlayback(
                    duration = audio.getDuration()?.toInt() ?: 0,
                    current = current.toInt(),
                    state = PlaybackStateCompat.STATE_PAUSED
                );
                _setNotification(audio);
            }

            override fun onSeek(audio: HYTAudioModel, duration: Int, to: Int) {
                _player?.isPlaying { playing: Boolean ->
                    val state: Int = when {
                        playing -> PlaybackStateCompat.STATE_PLAYING;
                        else -> PlaybackStateCompat.STATE_PAUSED
                    }
                    _setPlayback(
                        duration = duration,
                        current = to,
                        state = state
                    );
                    _setNotification(audio);
                }
            }

            private fun _setPlayback(duration: Int, current: Int, state: Int): Unit {
                _mediaSessionEditor { mediaSession,
                                      playbackState,
                                      metadataHolder ->
                    playbackState.setState(state, current.toLong(), 1.0f)
                    metadataHolder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration.toLong());
                    mediaSession.setPlaybackState(playbackState.build());
                    mediaSession.setMetadata(metadataHolder.build());
                }
            }

            private fun _setMetadata(audio: HYTAudioModel): Unit {
                _mediaSessionEditor { mediaSession: MediaSessionCompat,
                                      _,
                                      metadataHolder ->
                    metadataHolder
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, audio.getTitle())
                        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, audio.getArtist())
                        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, audio.getAlbum())
                        .putBitmap(
                            MediaMetadataCompat.METADATA_KEY_ALBUM_ART,
                            HYTUtil.getBitmap(audio.getAlbumPath(), contentResolver)
                        )
                        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, -1L)
                        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, audio.getAlbumPath()?.path)
                        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, audio.getDuration() ?: 0L);
                    mediaSession.setMetadata(
                        metadataHolder.build()
                    )
                };
            }

            private fun _setNotification(audio: HYTAudioModel): Unit {
                _notificationEditor { notificationHolder: NotificationCompat.Builder,
                                      manager: NotificationManager ->
                    notificationHolder
                        .setLargeIcon(HYTUtil.getBitmap(audio.getAlbumPath(), contentResolver))
                        .setContentTitle(audio.getTitle())
                        .setContentText(audio.getArtist());
                    manager.notify(1, notificationHolder.build());
                }
            }

        }
        _mainstream = resources.getString(R.string.hyt_mainstream);
        _player = HYTAudioPlayerFactory.getAudioPlayer(this);
        _binder.setPlayer(_player!!);
        _binder.addAuditor(_auditor);
        _preferencesAuditor?.onSharedPreferenceChanged(_preferences, respect);
        lifecycleScope.launch {
            _queueProvider!!.getByName(_mainstream!!) { manager: HYTAudioManager ->
                runBlocking {
                    _queueProvider!!.new { items: List<HYTAudioModel> ->
                        manager.queue { queue: MutableList<HYTAudioModel> ->
                            val setNext: Boolean = queue.isEmpty();
                            queue.addAll(
                                0,
                                items
                            );
                            if (setNext) {
                                manager.next { };
                            }
                            _player?.setManager(manager);
                        }
                    }
                }
            }
        }
        val destroyIntent: PendingIntent = HYTUtil.wrapIntentForService(
            this, Intent(_destroyIntent)
        );
        val open: Intent = Intent(this, HYTApp::class.java)
            .apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            };
        val openIntent: PendingIntent = PendingIntent.getActivity(
            this,
            100,
            open,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        );
        _mediaSessionEditor { mediaSession: MediaSessionCompat,
                              _, _ ->
            _notificationEditor { notificationHolder: NotificationCompat.Builder, _ ->
                notificationHolder
                    .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.hyt_empty_cover_200dp))
                    .setDeleteIntent(destroyIntent)
                    .setContentIntent(openIntent)
                    .setStyle(
                        DecoratedMediaCustomViewStyle()
                            .setShowCancelButton(true)
                            .setMediaSession(mediaSession.sessionToken)
                            .setCancelButtonIntent(destroyIntent)
                            .setShowActionsInCompactView(
                                0, 1, 2
                            )
                    )
                    .addAction(
                        NotificationCompat.Action.Builder(
                            R.drawable.hyt_player_previous_24dp, "previous", HYTUtil.wrapIntentForService(
                                this, Intent(_previousIntent)
                            )
                        ).build()
                    )
                    .addAction(
                        NotificationCompat.Action.Builder(
                            R.drawable.hyt_player_play_24dp, "play", HYTUtil.wrapIntentForService(
                                this, Intent(_playIntent)
                            )
                        ).build()
                    )
                    .addAction(
                        NotificationCompat.Action.Builder(
                            R.drawable.hyt_player_next_24dp, "next", HYTUtil.wrapIntentForService(
                                this, Intent(_nextIntent)
                            )
                        ).build()
                    )
                    .addAction(
                        NotificationCompat.Action.Builder(
                            R.drawable.hyt_destroy_24dp, "destroy", HYTUtil.wrapIntentForService(
                                this, Intent(_destroyIntent)
                            )
                        ).build()
                    )
                    .setSilent(true)
            }
        }
        _notificationEditor { notificationHolder: NotificationCompat.Builder, _ ->
            startForeground(1, notificationHolder.build());
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        _mediaSessionEditor { mediaSession: MediaSessionCompat,
                              _, _ ->
            MediaButtonReceiver.handleIntent(mediaSession, intent);
        }
        if (intent != null && intent.action != null) {
            when (intent.action!!) {
                _previousIntent -> _binder.previous();
                _playIntent -> {
                    _binder.isPlaying { playing ->
                        if (playing) {
                            _binder.pause();
                        } else {
                            _binder.play();
                        }
                    }
                }
                _nextIntent -> _binder.next();
                _destroyIntent -> stopSelf();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    override fun onDestroy() {
        runBlocking {
            _binder.save();
        }
        _preferences?.unregisterOnSharedPreferenceChangeListener(_preferencesAuditor);
        _binder.removeAuditor(_auditor);
        _binder.destroy();
        _player?.destroy();
        _mediaSessionEditor { mediaSession: MediaSessionCompat,
                              _, _ ->
            mediaSession.release();
        }
        _notificationEditor { _, manager: NotificationManager ->
            manager.cancelAll();
        }
        stopForeground(STOP_FOREGROUND_REMOVE);
        super.onDestroy();
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return _binder;
    }

}