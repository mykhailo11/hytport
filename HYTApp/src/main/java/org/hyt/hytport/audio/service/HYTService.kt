package org.hyt.hytport.audio.service

import android.app.*
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat;
import androidx.media.app.NotificationCompat.DecoratedMediaCustomViewStyle
import androidx.media.session.MediaButtonReceiver
import org.hyt.hytport.R
import org.hyt.hytport.audio.api.access.HYTAudioRepository
import org.hyt.hytport.audio.api.model.HYTAudioModel
import org.hyt.hytport.audio.api.service.HYTAudioPlayer
import org.hyt.hytport.audio.api.service.HYTBinder
import org.hyt.hytport.audio.factory.HYTAudioFactory
import org.hyt.hytport.audio.factory.HYTAudioPlayerFactory
import org.hyt.hytport.audio.util.HYTAudioUtil
import org.hyt.hytport.util.HYTUtil
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque

class HYTService : Service() {

    private lateinit var _binder: HYTBinder;

    private lateinit var _player: HYTAudioPlayer;

    private var _playIntent: String? = null;

    private var _nextIntent: String? = null;

    private var _previousIntent: String? = null;

    private var _destroyIntent: String? = null;

    private lateinit var _mediaSessionEditor: (
            (
            MediaSessionCompat,
            PlaybackStateCompat.Builder
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
        _binder = HYTAudioFactory.getBinder();
        _playIntent = resources.getString(R.string.hyt_service_play);
        _nextIntent = resources.getString(R.string.hyt_service_next);
        _previousIntent = resources.getString(R.string.hyt_service_previous);
        _destroyIntent = resources.getString(R.string.hyt_service_destroy);
        _mediaSessionEditor = HYTAudioUtil.mediaSession(
            javaClass.canonicalName!!,
            this,
            _binder
        );
        val repository: HYTAudioRepository = HYTAudioFactory.getAudioRepository(contentResolver);
        val queue: Deque<HYTAudioModel> = ConcurrentLinkedDeque();
        repository.getAllAudio {
            queue.addAll(it);
        }
        _player = HYTAudioPlayerFactory.getAudioPlayer(this)
        { queueConsumer: (Deque<HYTAudioModel>) -> Unit ->
            queueConsumer(queue);
        };
        val id: String = resources.getString(R.string.hyt_channel);
        val destroyIntent: PendingIntent = HYTUtil.wrapIntentForService(
            this, Intent(_destroyIntent)
        );
        _notificationEditor = HYTAudioUtil.notification(
            this,
            id,
            R.drawable.hyt_player_icon_200dp
        );
        _mediaSessionEditor { mediaSession: MediaSessionCompat,
                              _ ->
            _notificationEditor { notificationHolder: NotificationCompat.Builder, _ ->
                notificationHolder
                    .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.hyt_empty_cover_200dp))
                    .setDeleteIntent(destroyIntent)
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
            }
        }
        _auditor = object : HYTBinder.Companion.HYTAuditor {

            private var _metadataBuilder: MediaMetadataCompat.Builder? = null;

            override fun onReady(audio: HYTAudioModel) {
                _metadataBuilder = MediaMetadataCompat.Builder();
                _setMetadata(audio);
                _setNotification(audio);
            }

            override fun onNext(audio: HYTAudioModel) {
                _mediaSessionEditor { mediaSession: MediaSessionCompat,
                                      playbackHolder: PlaybackStateCompat.Builder ->
                    playbackHolder
                        .setState(PlaybackStateCompat.STATE_PLAYING, 0L, 1.0f)
                    mediaSession.setPlaybackState(playbackHolder.build());
                }
                _setMetadata(audio);
                _setNotification(audio);
            }

            override fun onPrevious(audio: HYTAudioModel) {
                _mediaSessionEditor { mediaSession: MediaSessionCompat,
                                      playbackHolder: PlaybackStateCompat.Builder ->
                    playbackHolder
                        .setState(PlaybackStateCompat.STATE_PLAYING, 0L, 1.0f)
                    mediaSession.setPlaybackState(playbackHolder.build());
                }
                _setMetadata(audio);
                _setNotification(audio);
            }

            override fun onPlay(audio: HYTAudioModel, current: Long) {
                _mediaSessionEditor { mediaSession: MediaSessionCompat,
                                      playbackHolder: PlaybackStateCompat.Builder ->
                    playbackHolder
                        .setState(PlaybackStateCompat.STATE_PLAYING, current, 1.0f)
                    mediaSession.setPlaybackState(playbackHolder.build());
                }
            }

            override fun onPause(audio: HYTAudioModel, current: Long) {
                _mediaSessionEditor { mediaSession: MediaSessionCompat,
                                      playbackHolder: PlaybackStateCompat.Builder ->
                    playbackHolder
                        .setState(PlaybackStateCompat.STATE_PAUSED, current, 1.0f)
                    mediaSession.setPlaybackState(playbackHolder.build());
                }
            }

            override fun progress(duration: Int, current: Int) {

            }

            private fun _setMetadata(audio: HYTAudioModel): Unit {
                if (_metadataBuilder != null) {
                    _mediaSessionEditor { mediaSession: MediaSessionCompat,
                                          _ ->
                        _metadataBuilder!!
                            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, audio.getTitle())
                            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, audio.getArtist())
                            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, audio.getAlbum())
                            .putBitmap(
                                MediaMetadataCompat.METADATA_KEY_ALBUM_ART,
                                HYTUtil.getBitmap(audio.getAlbumPath(), contentResolver)
                            )
                            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, audio.getAlbumPath()?.path)
                        val duration: Long? = audio.getDuration();
                        if (duration != null) {
                            _metadataBuilder!!.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration);
                        }
                        mediaSession.setMetadata(
                                _metadataBuilder!!.build()
                        )
                    };
                }



            }

            override fun onSetPlayer(player: HYTAudioPlayer) {
                _player.destroy();
                _player = player;
            }

            private fun _setNotification(audio: HYTAudioModel): Unit {
                _notificationEditor { notificationHolder: NotificationCompat.Builder,
                                      manager: NotificationManager ->
                    notificationHolder
                        .setLargeIcon(HYTUtil.getBitmap(audio.getAlbumPath(), contentResolver))
                        .setContentTitle(audio.getTitle())
                        .setContentText(audio.getArtist());
                    manager.notify(0, notificationHolder.build());
                }
            }

        }
        _binder.setPlayer(_player);
        _binder.addAuditor(_auditor);
        _notificationEditor { notificationHolder: NotificationCompat.Builder, _ ->
            startForeground(0, notificationHolder.build());
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        _mediaSessionEditor { mediaSession: MediaSessionCompat,
                              _ ->
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
        _binder.removeAuditor(_auditor);
        _binder.destroy();
        _player.destroy();
        _mediaSessionEditor { mediaSession: MediaSessionCompat,
                              _ ->
            mediaSession.release();
        }
        _notificationEditor { _, manager: NotificationManager ->
            manager.cancelAll();
        }
        stopForeground(STOP_FOREGROUND_REMOVE);
        super.onDestroy();
    }

    override fun onBind(intent: Intent?): IBinder {
        return _binder;
    }

}