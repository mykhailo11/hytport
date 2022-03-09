package org.hyt.hytport.audio.model

import android.content.ContentResolver
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import org.hyt.hytport.audio.api.model.HYTAudioModel
import org.hyt.hytport.audio.api.model.HYTAudioRepository
import org.hyt.hytport.audio.factory.HYTAudioFactory

class HYTBaseAudioRepository public constructor(resolver: ContentResolver) : HYTAudioRepository {

    companion object {

        private val _PROJECTION: Array<String> = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA
        );

        private val _QUERY__ID: String = "_id = ?";

        private val _QUERY_ARTIST: String = "artist = ?";

        private val _QUERY_ALBUM: String = "album = ?";

    }

    private val _resolver: ContentResolver;

    init {
        _resolver = resolver;
    }

    override fun getAllAudio(ready: (List<HYTAudioModel>) -> Unit): Unit {
        ready(_getAudio(
            null,
            null,
            _getStorage()
        ));
    }

    override fun getAudioById(id: Any, ready: (HYTAudioModel) -> Unit): Unit {
        val parameters: Array<String> = arrayOf(id.toString());
        ready(_getAudio(
            _QUERY__ID,
            parameters,
            _getStorage()
        ).first());
    }

    override fun getAudioByArtist(artist: String, ready: (List<HYTAudioModel>) -> Unit): Unit {
        val parameters: Array<String> = arrayOf(artist);
        ready(_getAudio(
            _QUERY_ARTIST,
            parameters,
            _getStorage()
        ));
    }

    override fun getAudioByAlbum(album: String, ready: (List<HYTAudioModel>) -> Unit): Unit {
        val parameters: Array<String> = arrayOf(album);
        ready(_getAudio(
            _QUERY_ALBUM,
            parameters,
            _getStorage()
        ));
    }

    private fun _getStorage(): Uri{
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }else{
            return MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        }
    }

    private fun _getAudio(
        query: String?,
        parameters: Array<String>?,
        storage: Uri
    ): List<HYTAudioModel> {
        val cursor: Cursor? = _resolver.query(
            storage,
            _PROJECTION,
            query,
            parameters,
            null
        );
        val items: MutableList<HYTAudioModel> = ArrayList();
        if (cursor != null && cursor.moveToFirst()) {
            val idColumn: Int = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
            val titleColumn: Int = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            val artistColumn: Int = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            val albumColumn: Int = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            val albumIdColumn: Int = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
            val durationColumn: Int = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            val dataColumn: Int = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            do {
                val audio: HYTAudioModel = HYTAudioFactory.getAudioModel();
                audio.setId(cursor.getLong(idColumn));
                audio.setTitle(cursor.getString(titleColumn));
                audio.setArtist(cursor.getString(artistColumn));
                audio.setAlbum(cursor.getString(albumColumn));
                val albumCursor: Cursor? = _resolver.query(
                    storage,
                    arrayOf(MediaStore.Audio.Albums._ID),
                    _QUERY__ID,
                    arrayOf(cursor.getLong(albumIdColumn).toString()),
                    null
                );
                if (albumCursor != null && albumCursor.moveToFirst()){
                    audio.setAlbumPath(
                        ContentUris.withAppendedId(
                            storage,
                            cursor.getLong(albumCursor.getColumnIndex(MediaStore.Audio.Albums._ID))
                        )
                    );
                }
                audio.setDuration(cursor.getLong(durationColumn));
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
                    audio.setPath(Uri.parse("file://" + cursor.getString(dataColumn)));
                }else{
                    audio.setPath(ContentUris.withAppendedId(storage, audio.getId()));
                }
                items.add(audio);
            } while (cursor.moveToNext());
        }
        cursor?.close();
        return items;
    }


}