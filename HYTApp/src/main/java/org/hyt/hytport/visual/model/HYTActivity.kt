package org.hyt.hytport.visual.model

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
import androidx.core.animation.doOnEnd
import org.hyt.hytport.R
import org.hyt.hytport.audio.api.model.HYTAudioModel
import org.hyt.hytport.audio.api.model.HYTAudioPlayer
import org.hyt.hytport.graphics.model.HYTCanvas
import org.hyt.hytport.visual.util.HYTAnimationUtil
import java.util.prefs.Preferences

class HYTActivity : HYTBaseActivity() {

    companion object {

        private val _STATES: Int = 6;

    }

    private lateinit var _surface: GLSurfaceView;

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

    private lateinit var _library: ImageButton;


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
        _surface = findViewById(R.id.HYTVisualizer);
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
        _library = findViewById(R.id.hyt_library_open);
        _initSurface(
            mapOf(
                Pair(resources.getString(R.string.pulse_states), pulseStates),
                Pair(resources.getString(R.string.balance_states), balanceStates),
            )
        );
        _initControls();
        _cover.clipToOutline = true;
    }

    override fun _getAudit(): HYTAudioPlayer.HYTAudioPlayerAudit {
        return object : HYTAudioPlayer.HYTAudioPlayerAudit {

            override fun getId(): Int {
                return _audit;
            }

            override fun setId(id: Int) {
                _audit = id;
            }

            override fun onReady() {
                super.onReady();
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

            override fun onDestroy(audio: HYTAudioModel) {
                _player.removeAudit(_audit);
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
        _surface.setOnClickListener {
            when (_modal.visibility) {
                View.GONE -> {
                    _modal.alpha = 0.0f;
                    _coverWrapper.translationY = -20.0f;
                    _controls.translationY = 20.0f;
                    _modal.visibility = View.VISIBLE;
                    val animator: ValueAnimator = HYTAnimationUtil.getAnimator {
                        val value: Float = it.animatedValue as Float
                        _modal.alpha = value;
                        _coverWrapper.translationY = (value - 1.0f) * 20.0f;
                        _controls.translationY = (1.0f - value) * 20.0f;
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
        _surface.setEGLContextClientVersion(3);
        val vertexShader: String = HYTUtil.readSource(resources.getString(R.string.vertex_shader), assets);
        val fragmentShader: String = HYTUtil.readSource(resources.getString(R.string.fragment_shader), assets);
        _surface.setRenderer(
            HYTCanvas(
                this,
                vertexShader,
                fragmentShader,
                states
            )
        );
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
        _library.setOnClickListener {
            startActivityIfNeeded(Intent(this, HYTLibrary::class.java), 100);
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