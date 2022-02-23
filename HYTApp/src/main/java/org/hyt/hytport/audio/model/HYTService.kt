package org.hyt.hytport.audio.model

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Binder
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import org.hyt.hytport.R
import org.hyt.hytport.audio.api.model.HYTAudioModel
import org.hyt.hytport.audio.api.model.HYTAudioPlayer
import org.hyt.hytport.audio.api.model.HYTAudioRepository
import org.hyt.hytport.audio.api.service.HYTBinder
import org.hyt.hytport.audio.factory.HYTAudioFactory
import org.hyt.hytport.audio.factory.HYTAudioPlayerFactory
import org.hyt.hytport.util.HYTUtil
import java.util.*

class HYTService: Service() {

    companion object{

        public val PREVIOUS: String = "org.hyt.hytport.PREVIOUS";

        public val PLAY: String = "org.hyt.hytport.PLAY";

        public val NEXT: String = "org.hyt.hytport.NEXT";

        public val DESTROY: String = "org.hyt.hytport.DESTROY";

    }

    private lateinit var _player: HYTAudioPlayer;

    private lateinit var _audioRepository: HYTAudioRepository;

    private lateinit var _binder: HYTBinder;

    override fun onCreate() {
        super.onCreate();
        _binder = HYTBaseBinder();
        _player = HYTAudioPlayerFactory.getAudioPlayer(this);
        /*_audioRepository = HYTAudioFactory.getRemoteAudioRepository(
            "https://mocki.io",
            mapOf(
                Pair(HYTRemoteAudioRepository.Companion.HYTEndpoints.GET_ALL, "v1/20f1c423-b54f-4594-915b-567f8aa99491"),
            ),
            this
        );*/
        _audioRepository = HYTAudioFactory.getAudioRepository(contentResolver);
        _audioRepository.getAllAudio { audios ->
            audios.forEach { audio ->
                _player.addNext(audio);
            };
            _player.next();
        };
        val notificationChannel: NotificationChannel = NotificationChannel(
            "hyt_channel",
            "HYT Channel",
            NotificationManager.IMPORTANCE_HIGH
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
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setColor(Color.YELLOW)
            .setCustomContentView(playerView)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build();
        startForeground(100, notification);
    }

    private fun _initViewIntents(playerView: RemoteViews): Unit{
        playerView.setOnClickPendingIntent(
            R.id.hyt_player_previous,
            HYTUtil.wrapIntentForService(
                this, Intent(PREVIOUS)
            )
        );
        playerView.setOnClickPendingIntent(
            R.id.hyt_player_play,
            HYTUtil.wrapIntentForService(
                this, Intent(PLAY)
            )
        );
        playerView.setOnClickPendingIntent(
            R.id.hyt_player_next,
            HYTUtil.wrapIntentForService(
                this, Intent(NEXT)
            )
        );
        playerView.setOnClickPendingIntent(
            R.id.hyt_player_close,
            HYTUtil.wrapIntentForService(
                this, Intent(DESTROY)
            )
        );
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null){
            when (intent.action){
                PREVIOUS -> _player.previous();
                PLAY -> {
                    if (_player.isPlaying()){
                        _player.pause();
                    }else{
                        _player.play();
                    }
                }
                NEXT -> _player.next();
                DESTROY -> stopSelf();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    override fun onDestroy() {
        _player.destroy();
        super.onDestroy();
    }

    private inner class HYTBaseBinder: Binder(), HYTBinder{

        override fun play(): HYTAudioModel {
            return _player.play();
        }

        override fun play(audio: HYTAudioModel): HYTAudioModel {
            return _player.play(audio);
        }

        override fun isPlaying(): Boolean {
            return _player.isPlaying();
        }

        override fun pause(): HYTAudioModel {
            return _player.pause();
        }

        override fun next(): HYTAudioModel {
            return _player.next();
        }

        override fun previous(): HYTAudioModel {
            return _player.previous();
        }

        override fun addNext(next: HYTAudioModel) {
            _player.addNext(next);
        }

        override fun queue(): Deque<HYTAudioModel> {
            return _player.queue();
        }

        override fun destroy() {
            _player.destroy();
        }

        override fun addAudit(audit: HYTAudioPlayer.HYTAudioPlayerAudit): Int {
            return _player.addAudit(audit);
        }

        override fun removeAudit(audit: Int) {
            _player.removeAudit(audit);
        }

    }

    override fun onBind(intent: Intent?): IBinder {
        return _binder;
    }




}