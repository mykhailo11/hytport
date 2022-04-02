package org.hyt.hytport.audio.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.session.MediaSession
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
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


    private lateinit var _mediaSession: MediaSession;

    override fun onCreate() {
        super.onCreate();
        _binder = HYTAudioFactory.getBinder();
        _playIntent = resources.getString(R.string.hyt_service_play);
        _nextIntent = resources.getString(R.string.hyt_service_next);
        _previousIntent = resources.getString(R.string.hyt_service_previous);
        _destroyIntent = resources.getString(R.string.hyt_service_destroy);
        _mediaSession = HYTAudioUtil.mediaSession(
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
        _binder.setPlayer(_player);
        val id: String = resources.getString(R.string.hyt_channel);
        val notificationChannel: NotificationChannel = NotificationChannel(
            id,
            id,
            NotificationManager.IMPORTANCE_DEFAULT
        );
        notificationChannel.lightColor = Color.YELLOW;
        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC;
        val notificationManager: NotificationManager = getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager;
        notificationManager.createNotificationChannel(notificationChannel);
        val playerView: RemoteViews = RemoteViews("org.hyt.hytport", R.layout.hyt_player);
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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
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
        _binder.destroy();
        _player.destroy();
        _mediaSession.release();
        super.onDestroy();
    }

    override fun onBind(intent: Intent?): IBinder {
        return _binder;
    }

}