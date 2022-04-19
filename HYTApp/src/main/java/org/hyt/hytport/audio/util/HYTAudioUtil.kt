package org.hyt.hytport.audio.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import com.spotify.protocol.types.Artist
import com.spotify.protocol.types.Track
import org.hyt.hytport.audio.api.model.HYTAudioManager
import org.hyt.hytport.audio.api.model.HYTAudioModel
import org.hyt.hytport.audio.api.service.HYTAudioPlayer
import org.hyt.hytport.audio.factory.HYTAudioFactory

class HYTAudioUtil {

    companion object {

        public fun mapToAudioModel(track: Track): HYTAudioModel {
            return HYTAudioFactory.getAudioModel().apply {
                setPath(Uri.parse(track.uri));
                setDuration(track.duration)
                setTitle(track.name)
                setAlbum(track.album.name)
                setArtist(track.artists.joinToString { artist: Artist? ->
                    artist!!.name
                });
            };
        }

        public fun mediaSession(id: String, context: Service, player: HYTAudioPlayer): (
                (
                MediaSessionCompat,
                PlaybackStateCompat.Builder,
                MediaMetadataCompat.Builder
            ) -> Unit
        ) -> Unit {
            val mediaSession: MediaSessionCompat = MediaSessionCompat(
                context,
                id
            );
            val playbackStateHolder: PlaybackStateCompat.Builder = PlaybackStateCompat.Builder()
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY or
                            PlaybackStateCompat.ACTION_PAUSE or
                            PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                );
            val metadataHolder: MediaMetadataCompat.Builder = MediaMetadataCompat.Builder();
            mediaSession.setCallback(
                object : MediaSessionCompat.Callback() {

                    override fun onPlay() {
                        player.play()
                    }

                    override fun onSkipToQueueItem(id: Long) {
                        player.manger { manager: HYTAudioManager ->
                            manager.current(
                                HYTAudioFactory.getAudioModel().apply {
                                    setId(id);
                                }
                            );
                            manager.current { current: HYTAudioModel ->
                                player.play(current);
                            }
                        }
                    }

                    override fun onPause() {
                        player.pause();
                    }

                    override fun onSkipToNext() {
                        player.next();
                    }

                    override fun onSkipToPrevious() {
                        player.previous();
                    }

                    override fun onStop() {
                        context.stopSelf()
                    }

                }
            );
            mediaSession.setPlaybackState(playbackStateHolder.build());
            mediaSession.setMetadata(metadataHolder.build());
            mediaSession.isActive = true;
            return { consumer: (
                MediaSessionCompat,
                PlaybackStateCompat.Builder,
                MediaMetadataCompat.Builder
            ) -> Unit ->
                consumer(mediaSession, playbackStateHolder, metadataHolder);
            }
        }

        public fun notification(context: Service, id: String, icon: Int): (
                (
                NotificationCompat.Builder,
                NotificationManager
            ) -> Unit
        ) -> Unit {
            val notificationChannel: NotificationChannel = NotificationChannel(
                id,
                id,
                NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationChannel.lightColor = Color.YELLOW;
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC;
            val notificationManager: NotificationManager = context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager;
            notificationManager.createNotificationChannel(notificationChannel);
            val notificationHolder: NotificationCompat.Builder = NotificationCompat.Builder(
                context,
                id
            )
                .setSmallIcon(icon)
                .setOnlyAlertOnce(true)
                .setShowWhen(false)
                .setColorized(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            return { consumer: (NotificationCompat.Builder, NotificationManager) -> Unit ->
                consumer(notificationHolder, notificationManager);
            }
        }

    }

}