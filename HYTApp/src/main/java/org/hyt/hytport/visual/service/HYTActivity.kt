package org.hyt.hytport.visual.service

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import org.hyt.hytport.util.HYTMathUtil
import org.hyt.hytport.util.HYTUtil
import org.hyt.hytport.visual.api.model.HYTState
import org.hyt.hytport.visual.factory.HYTStateFactory
import android.animation.ValueAnimator
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.view.MotionEvent
import android.widget.*
import androidx.activity.viewModels
import androidx.core.animation.doOnEnd
import org.hyt.hytport.R
import org.hyt.hytport.audio.api.model.HYTAudioModel
import org.hyt.hytport.audio.api.service.HYTAudioPlayer
import org.hyt.hytport.audio.api.service.HYTBinder
import org.hyt.hytport.graphics.service.HYTCanvas
import org.hyt.hytport.visual.fragment.visualizer.HYTVisualizerModel
import org.hyt.hytport.visual.util.HYTAnimationUtil

class HYTActivity : HYTBaseActivity() {

    companion object {

        private val _STATES: Int = 6;

    }

    private val _visualizerModel: HYTVisualizerModel by viewModels();

    private lateinit var _title: TextView;

    private lateinit var _artist: TextView;

    private lateinit var _modal: ConstraintLayout;

    private lateinit var _controls: ConstraintLayout;

    private lateinit var _cover: ImageButton;

    private lateinit var _coverWrapper: ConstraintLayout;

    private lateinit var _next: ImageView;

    private lateinit var _previous: ImageView;

    private lateinit var _play: ImageView;

    private lateinit var _meta: ConstraintLayout;

    private lateinit var _consumer: (ByteArray) -> Unit;


    override fun onCreate(savedInstanceState: Bundle?) {
        val pulseStates: Array<HYTState> = Array(_STATES) {
            HYTStateFactory.getPulseState(0.02f);
        };
        val balanceStates: Array<HYTState> = Array(_STATES) {
            HYTStateFactory.getBalanceState(0.02f);
        }
        _consumer = { food: ByteArray ->
            val result: Float = HYTMathUtil.getNormalBytesAverage(food);
            val chosen: Int = (Math.random() * _STATES).toInt();
            balanceStates[chosen].setState(result);
            if (result > 0.01){
                pulseStates[chosen].setState(result);
            }
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hyt_activity);
        _title = findViewById(R.id.hyt_meta_title);
        _artist = findViewById(R.id.hyt_meta_artist);
        _modal = findViewById(R.id.hyt_modal);
        _controls = findViewById(R.id.hyt_controls);
        _cover = findViewById(R.id.hyt_cover);
        _coverWrapper = findViewById(R.id.hyt_cover_wrapper);
        _next = findViewById(R.id.hyt_next);
        _previous = findViewById(R.id.hyt_previous);
        _play = findViewById(R.id.hyt_play);
        _meta = findViewById(R.id.hyt_meta_container);
        _initSurface(
            mapOf(
                Pair(resources.getString(R.string.pulse_states), pulseStates),
                Pair(resources.getString(R.string.balance_states), balanceStates),
            )
        );
        _initControls();
        _cover.clipToOutline = true;
    }

    override fun _getAuditor(): HYTBinder.Companion.HYTAuditor {
        return object : HYTBinder.Companion.HYTAuditor {

            private var _id: Long = -1L;

            override fun getId(): Long{
                return _id;
            }

            override fun setId(id: Long) {
                _id = id;
            }

            override fun onReady() {
                _player.queue {
                    if (!it.isEmpty()){
                        _setMeta(it.last);
                    }
                }
            }

            override fun onPlay(audio: HYTAudioModel) {
                _play.setImageResource(R.drawable.hyt_play_200dp);
                _setMeta(audio);
            }

            override fun onPause(audio: HYTAudioModel) {
                _play.setImageResource(R.drawable.hyt_pause_200dp);
                _setMeta(audio);
            }

            override fun onNext(audio: HYTAudioModel) {
                onPlay(audio);
            }

            override fun onPrevious(audio: HYTAudioModel) {
                onPlay(audio);
            }

            override fun consumer(food: ByteArray) {
                _consumer.invoke(food);
            }

            override fun onDestroy() {
                _player.removeAuditor(_auditor!!);
                _play.setImageResource(R.drawable.hyt_pause_200dp);
            }

        }
    }

    private fun _setMeta(audio: HYTAudioModel): Unit {
        val cover: Bitmap? = HYTUtil.getBitmap(audio.getAlbumPath(), contentResolver);
        if (cover != null){
            _cover.setImageBitmap(cover);
        }else{
            _cover.setImageResource(R.drawable.hyt_empty_cover_200dp);
        }
        _title.text = audio.getTitle();
        _artist.text = audio.getArtist();
    }

    private fun _initSurface(states: Map<String, Array<HYTState>>): Unit {
        _visualizerModel.setSurfaceClick {
            when (_modal.visibility) {
                View.GONE -> {
                    _modal.alpha = 0.0f;
                    _coverWrapper.translationY = -30.0f;
                    _controls.translationY = 30.0f;
                    _modal.visibility = View.VISIBLE;
                    val animator: ValueAnimator = HYTAnimationUtil.getAnimator {
                        val value: Float = it.animatedValue as Float
                        _modal.alpha = value;
                        _coverWrapper.translationY = (value - 1.0f) * 30.0f;
                        _controls.translationY = (1.0f - value) * 30.0f;
                    };
                    animator.start();
                };
                View.VISIBLE -> {
                    _modal.alpha = 1.0f;
                    val animator: ValueAnimator = HYTAnimationUtil.getAnimator {
                        val value: Float = it.animatedValue as Float
                        _modal.alpha = 1.0f - value;
                    };
                    animator.doOnEnd {
                        _modal.visibility = View.GONE;
                    }
                    animator.start();
                };
                View.INVISIBLE -> _modal.visibility = View.VISIBLE
            }
        }
        _visualizerModel.setSurfaceLongClick {
            startActivityIfNeeded(Intent(this, HYTLibrary::class.java), 100);
        }
        _visualizerModel.vertexShader = HYTUtil.readSource(resources.getString(R.string.vertex_shader), assets);
        _visualizerModel.fragmentShader = HYTUtil.readSource(resources.getString(R.string.fragment_shader), assets);
        _visualizerModel.states = states;
    }

    private fun _initControls(): Unit {
        _next.setOnTouchListener { view: View, motion: MotionEvent ->
            _startPlayer();
            if (motion.action == MotionEvent.ACTION_DOWN && _bound) {
                _next.setImageResource(R.drawable.hyt_next_press200dp);
                _play.setImageResource(R.drawable.hyt_play_200dp);
                _player.next();
            } else if (motion.action == MotionEvent.ACTION_UP && _bound) {
                _next.setImageResource(R.drawable.hyt_next_200dp);
            }
            view.performClick();
        }
        _previous.setOnTouchListener { view: View, motion: MotionEvent ->
            _startPlayer();
            if (motion.action == MotionEvent.ACTION_DOWN && _bound) {
                _previous.setImageResource(R.drawable.hyt_next_press200dp);
                _play.setImageResource(R.drawable.hyt_play_200dp);
                _player.previous();
            } else if (motion.action == MotionEvent.ACTION_UP && _bound) {
                _previous.setImageResource(R.drawable.hyt_next_200dp);
            }
            view.performClick();
        }
        _play.setOnTouchListener { view: View, motion: MotionEvent ->
            _startPlayer()
            if (_bound && _player.isPlaying() && motion.action == MotionEvent.ACTION_DOWN) {
                _play.setImageResource(R.drawable.hyt_pause_200dp);
                _player.pause();

            } else if (motion.action == MotionEvent.ACTION_DOWN && _bound) {
                _play.setImageResource(R.drawable.hyt_play_200dp);
                _player.play();
            }
            view.performClick();
        }
        _cover.setOnTouchListener{ view: View, motion: MotionEvent ->
            if (motion.action == MotionEvent.ACTION_DOWN){
                HYTAnimationUtil.animateVisibility(_meta);
            }
            view.performClick();
        }
    }

    override fun onConfigurationChanged(configuration: Configuration) {
        super.onConfigurationChanged(configuration);
        when (configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {

            }
            Configuration.ORIENTATION_PORTRAIT -> {

            }
        };
    }

}