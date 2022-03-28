package org.hyt.hytport.visual.service

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.core.widget.addTextChangedListener
import org.hyt.hytport.R
import org.hyt.hytport.audio.api.model.HYTAudioModel
import org.hyt.hytport.audio.factory.HYTAudioFactory
import org.hyt.hytport.audio.access.HYTBaseAudioRepository
import org.hyt.hytport.audio.access.HYTRemoteAudioRepository
import org.hyt.hytport.audio.api.access.HYTAudioRepository
import org.hyt.hytport.audio.api.service.HYTBinder
import org.hyt.hytport.visual.factory.HYTViewFactory

class HYTLibrary : HYTBaseActivity() {

    private var _list: ListView? = null;

    private var _switch: Button? = null;

    private var _search: EditText? = null;

    private lateinit var _backPlayer: ImageButton;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hyt_library);
        _list = findViewById(R.id.hyt_songs);
        _list!!.divider = null;
        _list!!.dividerHeight = 0;
        _switch = findViewById(R.id.hyt_switch);
        _backPlayer = findViewById(R.id.hyt_player_back);
        _search = findViewById(R.id.hyt_audio_search);
        _search!!.setOnFocusChangeListener { view: View, focused: Boolean ->
            if (!focused){
                val text: String = _search!!.text.toString();
                (_list!!.adapter as Filterable).filter.filter(text);
            }
        }
        _initControls();
    }

    private fun _initControls(): Unit {
        _switch!!.setOnClickListener {
            val repository: Class<HYTAudioRepository>? = _player.getRepository();
            if (repository != null){
                when (repository.canonicalName) {
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
            }
        };
        _backPlayer.setOnTouchListener { view: View, motion: MotionEvent ->
            if (motion.action == MotionEvent.ACTION_DOWN) {
                finish();
            }
            view.performClick();
        }
    }

    override fun _getAuditor(): HYTBinder.Companion.HYTAuditor {
        val context: Context = this;
        return object : HYTBinder.Companion.HYTAuditor {

            private var _id: Long = -1L;

            override fun getId(): Long {
                return _id;
            }

            override fun setId(id: Long) {
                _id = id;
            }

            override fun onReady() {
                _player.queue {
                    _list!!.adapter = HYTViewFactory.getAudioAdapter(context, it) { audio: HYTAudioModel ->
                        _player.play(audio);
                    };
                    _updateQueue();
                }
            }

            override fun onAddNext(audio: HYTAudioModel) {
                if (_list!!.adapter != null){
                    _updateQueue();
                }
            }

            override fun onRepositoryChanged(repository: HYTAudioRepository) {
                _updateQueue();
            }

        };
    }

    private fun _updateQueue(): Unit {
        val repository: Class<HYTAudioRepository>? = _player.getRepository();
        when (repository!!.canonicalName){
            HYTBaseAudioRepository::class.java.canonicalName -> _switch!!.setTextColor(
                this.getColor(R.color.hyt_text_dark)
            );
            HYTRemoteAudioRepository::class.java.canonicalName -> _switch!!.setTextColor(
                this.getColor(R.color.hyt_grey)
            );
        }
        (_list!!.adapter as BaseAdapter).notifyDataSetChanged();
    }

}