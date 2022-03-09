package org.hyt.hytport.visual.model

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ListView
import org.hyt.hytport.R
import org.hyt.hytport.audio.api.model.HYTAudioModel
import org.hyt.hytport.audio.api.model.HYTAudioPlayer
import org.hyt.hytport.audio.factory.HYTAudioFactory
import org.hyt.hytport.audio.model.HYTBaseAudioRepository
import org.hyt.hytport.audio.model.HYTRemoteAudioRepository
import org.hyt.hytport.visual.factory.HYTViewFactory

class HYTLibrary : HYTBaseActivity() {

    private lateinit var _list: ListView;

    private lateinit var _switch: Button;

    private lateinit var _backPlayer: ImageButton;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hyt_library);
        _list = findViewById(R.id.hyt_songs);
        _list.divider = null;
        _list.dividerHeight = 0;
        _switch = findViewById(R.id.hyt_switch);
        _backPlayer = findViewById(R.id.hyt_player_back);
        _initControls();
    }

    private fun _initControls(): Unit {
        _switch.setOnClickListener {
            when (_player.getRepository()) {
                HYTBaseAudioRepository::class.java.canonicalName -> _player.setRepository(
                    HYTAudioFactory.getRemoteAudioRepository(
                        resources.getString(R.string.remote_service_base),
                        mapOf(
                            Pair(HYTRemoteAudioRepository.Companion.HYTEndpoints.GET_ALL, "all")
                        ),
                        this
                    )
                );
                HYTRemoteAudioRepository::class.java.canonicalName -> _player.setRepository(
                    HYTAudioFactory.getAudioRepository(contentResolver)
                );
            }
        };
        _backPlayer.setOnTouchListener { view: View, motion: MotionEvent ->
            if (motion.action == MotionEvent.ACTION_DOWN) {
                finish();
            }
            view.performClick();
        }
    }

    override fun _getAudit(): HYTAudioPlayer.HYTAudioPlayerAudit {
        val context: Context = this;
        return object : HYTAudioPlayer.HYTAudioPlayerAudit {

            override fun getId(): Int {
                return _audit;
            }

            override fun setId(id: Int) {
                _audit = id;
            }

            override fun onReady() {
                _list.adapter = HYTViewFactory.getAudioAdapter(context, _player.queue()) {
                    _player.play(it);
                };
                _updateQueue();
            }

            override fun onAddNext(audio: HYTAudioModel) {
                _updateQueue();
            }

        };
    }

    private fun _updateQueue(): Unit {
        when (_player.getRepository()){
            HYTBaseAudioRepository::class.java.canonicalName -> _switch.setTextColor(
                this.getColor(R.color.hyt_text_dark)
            );
            HYTRemoteAudioRepository::class.java.canonicalName -> _switch.setTextColor(
              this.getColor(R.color.hyt_grey)
            );
        }
        (_list.adapter as BaseAdapter).notifyDataSetChanged();
    }

}