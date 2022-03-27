package org.hyt.hytport.visual.fragment.player

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import org.hyt.hytport.R
import org.hyt.hytport.audio.api.model.HYTAudioModel
import org.hyt.hytport.audio.api.service.HYTBinder
import java.util.*

class HYTPlayer : Fragment(R.layout.hyt_player_fragment) {

    private val _model: HYTPlayerModel by activityViewModels();

    private var _title: TextView? = null;

    private var _artist: TextView? = null;

    private var _next: ImageButton? = null;

    private var _previous: ImageButton? = null;

    private var _play: ImageButton? = null;

    private var _auditor: HYTBinder.Companion.HYTAuditor? = null;

    private var _player: HYTBinder? = null;

    private var _bound: Boolean = false;

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _title = view.findViewById(R.id.hyt_meta_title);
        _artist = view.findViewById(R.id.hyt_meta_artist);
        _next = view.findViewById(R.id.hyt_next);
        _previous = view.findViewById(R.id.hyt_previous);
        _play = view.findViewById(R.id.hyt_play);
        _model.player.observe(this) { player: HYTBinder ->
            if (_auditor == null){
                _player = player;
                _bound = true;
                _auditor = object : HYTBinder.Companion.HYTAuditor {

                    private var _id: Long = -1L;

                    override fun getId(): Long {
                        return _id;
                    }

                    override fun setId(id: Long) {
                        _id = id;
                    }

                    override fun onReady() {
                        player.queue { queue: Deque<HYTAudioModel> ->
                            if (!queue.isEmpty()) {
                                _setMeta(queue.last);
                            }
                        }
                        if (player.isPlaying()){
                            _play!!.setImageResource(R.drawable.hyt_play_200dp);
                        }else {
                            _play!!.setImageResource(R.drawable.hyt_pause_200dp);
                        }
                    }

                    override fun onPlay(audio: HYTAudioModel) {
                        _play!!.setImageResource(R.drawable.hyt_play_200dp);
                        _setMeta(audio);
                    }

                    override fun onPause(audio: HYTAudioModel) {
                        _play!!.setImageResource(R.drawable.hyt_pause_200dp);
                        _setMeta(audio);
                    }

                    override fun onNext(audio: HYTAudioModel) {
                        onPlay(audio);
                    }

                    override fun onPrevious(audio: HYTAudioModel) {
                        onPlay(audio);
                    }

                    override fun onDestroy() {
                        player.removeAuditor(this);
                        _bound = false;
                        _play!!.setImageResource(R.drawable.hyt_pause_200dp);
                    }

                }
                _player!!.addAuditor(_auditor!!);
            }
        }
        _initControls();
    }

    private fun _setMeta(audio: HYTAudioModel): Unit {
        _title!!.text = audio.getTitle();
        _artist!!.text = audio.getArtist();
    }

    private fun _initControls(): Unit {
        _next!!.setOnTouchListener { view: View, motion: MotionEvent ->
            _model.start();
            if (motion.action == MotionEvent.ACTION_DOWN && _bound) {
                _next!!.setImageResource(R.drawable.hyt_next_press200dp);
                _play!!.setImageResource(R.drawable.hyt_play_200dp);
                _player!!.next();
            } else if (motion.action == MotionEvent.ACTION_UP && _bound) {
                _next!!.setImageResource(R.drawable.hyt_next_200dp);
            }
            view.performClick();
        }
        _previous!!.setOnTouchListener { view: View, motion: MotionEvent ->
            _model.start();
            if (motion.action == MotionEvent.ACTION_DOWN && _bound) {
                _previous!!.setImageResource(R.drawable.hyt_next_press200dp);
                _play!!.setImageResource(R.drawable.hyt_play_200dp);
                _player!!.previous();
            } else if (motion.action == MotionEvent.ACTION_UP && _bound) {
                _previous!!.setImageResource(R.drawable.hyt_next_200dp);
            }
            view.performClick();
        }
        _play!!.setOnTouchListener { view: View, motion: MotionEvent ->
            _model.start();
            if (_bound && _player!!.isPlaying() && motion.action == MotionEvent.ACTION_DOWN) {
                _play!!.setImageResource(R.drawable.hyt_pause_200dp);
                _player!!.pause();

            } else if (motion.action == MotionEvent.ACTION_DOWN && _bound) {
                _play!!.setImageResource(R.drawable.hyt_play_200dp);
                _player!!.play();
            }
            view.performClick();
        }
    }

    override fun onDestroy() {
        if (_player != null && _auditor != null) {
            _player!!.removeAuditor(_auditor!!);
        }
        super.onDestroy()
    }
}