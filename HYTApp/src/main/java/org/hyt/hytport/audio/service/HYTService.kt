package org.hyt.hytport.audio.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Binder
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import org.hyt.hytport.R
import org.hyt.hytport.audio.api.model.HYTAudioModel
import org.hyt.hytport.audio.api.service.HYTAudioPlayer
import org.hyt.hytport.audio.api.access.HYTAudioRepository
import org.hyt.hytport.audio.api.service.HYTBinder
import org.hyt.hytport.audio.factory.HYTAudioFactory
import org.hyt.hytport.audio.factory.HYTAudioPlayerFactory
import org.hyt.hytport.util.HYTUtil
import java.util.*
import kotlin.collections.ArrayList

class HYTService : Service() {

    private lateinit var _binder: HYTBinder;

    private lateinit var _mediaSession: MediaSession;

    private var _playIntent: String? = null;

    private var _nextIntent: String? = null;

    private var _previousIntent: String? = null;

    private var _destroyIntent: String? = null;

    override fun onCreate() {
        super.onCreate();
        _playIntent = resources.getString(R.string.hyt_service_play);
        _nextIntent = resources.getString(R.string.hyt_service_next);
        _previousIntent = resources.getString(R.string.hyt_service_previous);
        _destroyIntent = resources.getString(R.string.hyt_service_destroy);
        _binder = HYTAudioFactory.getBinder(HYTAudioPlayerFactory.getAudioPlayer(this) {
            _binder.next();
        });
        _mediaSession = MediaSession(this, "hyt_session");
        _mediaSession.setCallback(object : MediaSession.Callback() {

            override fun onPlay() {
                _binder.play()
            }

            override fun onPause() {
                _binder.pause();
            }

            override fun onSkipToNext() {
                _binder.next();
            }

            override fun onSkipToPrevious() {
                _binder.previous();
            }

        });
        _mediaSession.setPlaybackState(
            PlaybackState.Builder()
                .setActions(
                    PlaybackState.ACTION_PLAY or
                            PlaybackState.ACTION_PAUSE or
                            PlaybackState.ACTION_SKIP_TO_NEXT or
                            PlaybackState.ACTION_SKIP_TO_PREVIOUS
                )
                .build()
        )
        val notificationChannel: NotificationChannel = NotificationChannel(
            "hyt_channel",
            "HYT Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        );
        notificationChannel.lightColor = Color.YELLOW;
        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC;
        val notificationManager: NotificationManager = getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager;
        notificationManager.createNotificationChannel(notificationChannel);
        val playerView: RemoteViews = RemoteViews("org.hyt.hytport", R.layout.hyt_player);
        _initViewIntents(playerView);
        val notification: Notification = NotificationCompat.Builder(
            this,
            "hyt_channel"
        )
            .setSmallIcon(R.drawable.hyt_player_icon_200dp)
            .setColor(Color.YELLOW)
            .setCustomContentView(playerView)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build();
        startForeground(100, notification);
    }

    private fun _initViewIntents(playerView: RemoteViews): Unit {
        playerView.setOnClickPendingIntent(
            R.id.hyt_player_previous,
            HYTUtil.wrapIntentForService(
                this, Intent(_previousIntent)
            )
        );
        playerView.setOnClickPendingIntent(
            R.id.hyt_player_play,
            HYTUtil.wrapIntentForService(
                this, Intent(_playIntent)
            )
        );
        playerView.setOnClickPendingIntent(
            R.id.hyt_player_next,
            HYTUtil.wrapIntentForService(
                this, Intent(_nextIntent)
            )
        );
        playerView.setOnClickPendingIntent(
            R.id.hyt_player_close,
            HYTUtil.wrapIntentForService(
                this, Intent(_destroyIntent)
            )
        );
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null && intent.action != null) {
            when (intent.action!!) {
                _previousIntent -> _binder.previous();
                _playIntent -> {
                    if (_binder.isPlaying()) {
                        _binder.pause();
                    } else {
                        _binder.play();
                    }
                }
                _nextIntent -> _binder.next();
                _destroyIntent -> stopSelf();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    override fun onDestroy() {
        _binder.destroy();
        _mediaSession.release();
        super.onDestroy();
    }

    override fun onBind(intent: Intent?): IBinder {
        return _binder;
    }

}