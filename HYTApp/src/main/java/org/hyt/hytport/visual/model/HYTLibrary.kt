package org.hyt.hytport.visual.model

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.AudioManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.view.Window
import android.widget.BaseAdapter
import android.widget.ListAdapter
import android.widget.ListView
import org.hyt.hytport.R
import org.hyt.hytport.audio.api.model.HYTAudioModel
import org.hyt.hytport.audio.api.model.HYTAudioPlayer
import org.hyt.hytport.audio.api.service.HYTBinder
import org.hyt.hytport.audio.model.HYTService
import org.hyt.hytport.visual.factory.HYTViewFactory

class HYTLibrary : HYTBaseActivity() {

    private lateinit var _list: ListView;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hyt_library);
        _list = findViewById(R.id.hyt_songs);
    }

    override fun _getAudit(): HYTAudioPlayer.HYTAudioPlayerAudit {
        val context: Context = this;
        return object: HYTAudioPlayer.HYTAudioPlayerAudit{

            override fun onReady() {
                 _list.adapter = HYTViewFactory.getAudioAdapter(context, _player.queue()){
                     _player.play(it);
                 };
                _updateQueue();
            }

            override fun onAddNext(audio: HYTAudioModel) {
                _updateQueue();
            }

        };
    }

    private fun _updateQueue(): Unit{
        (_list.adapter as BaseAdapter).notifyDataSetChanged();
    }

}