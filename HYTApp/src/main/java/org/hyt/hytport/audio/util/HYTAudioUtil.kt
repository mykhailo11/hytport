package org.hyt.hytport.audio.util

import android.content.Context
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.net.Uri
import com.spotify.protocol.types.Artist
import com.spotify.protocol.types.Track
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

        public fun mediaSession(id: String, context: Context, player: HYTAudioPlayer): MediaSession {
            val mediaSession: MediaSession = MediaSession(
                context,
                id
            );
            mediaSession.setCallback(
                object: MediaSession.Callback() {

                    override fun onPlay() {
                        player.play()
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

                }
            );
            mediaSession.setPlaybackState(
                PlaybackState.Builder()
                    .setActions(
                        PlaybackState.ACTION_PLAY or
                                PlaybackState.ACTION_PAUSE or
                                PlaybackState.ACTION_SKIP_TO_NEXT or
                                PlaybackState.ACTION_SKIP_TO_PREVIOUS
                    )
                    .build()
            );
            return mediaSession;
        }

    }

}