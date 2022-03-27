package org.hyt.hytport.visual.service

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
import android.widget.*
import androidx.activity.viewModels
import androidx.core.animation.doOnEnd
import org.hyt.hytport.R
import org.hyt.hytport.audio.api.model.HYTAudioModel
import org.hyt.hytport.audio.api.service.HYTBinder
import org.hyt.hytport.visual.fragment.player.HYTPlayerModel
import org.hyt.hytport.visual.fragment.visualizer.HYTVisualizerModel
import org.hyt.hytport.visual.util.HYTAnimationUtil

class HYTActivity : HYTBaseActivity() {

    companion object {

        private val _STATES: Int = 6;

    }

    private val _visualizerModel: HYTVisualizerModel by viewModels();

    private val _playerModel: HYTPlayerModel by viewModels();

    private lateinit var _modal: ConstraintLayout;

    private lateinit var _cover: ImageView;

    private lateinit var _coverWrapper: ConstraintLayout;

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
        _modal = findViewById(R.id.hyt_modal);
        _cover = findViewById(R.id.hyt_cover);
        _coverWrapper = findViewById(R.id.hyt_cover_wrapper);
        _initSurface(
            mapOf(
                Pair(resources.getString(R.string.pulse_states), pulseStates),
                Pair(resources.getString(R.string.balance_states), balanceStates),
            )
        );
        _cover.clipToOutline = true;
        _playerModel.start = {
            _startPlayer();
        }
    }

    override fun _preparePlayer() {
        _playerModel.player.value = _player;
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
                _setMeta(audio);
            }

            override fun onPause(audio: HYTAudioModel) {
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

        }
    }

    private fun _setMeta(audio: HYTAudioModel): Unit {
        val cover: Bitmap? = HYTUtil.getBitmap(audio.getAlbumPath(), contentResolver);
        if (cover != null){
            _cover.setImageBitmap(cover);
        }else{
            _cover.setImageResource(R.drawable.hyt_empty_cover_200dp);
        }
    }

    private fun _initSurface(states: Map<String, Array<HYTState>>): Unit {
        _visualizerModel.click.value = {
            when (_modal.visibility) {
                View.GONE -> {
                    _modal.alpha = 0.0f;
                    _modal.visibility = View.VISIBLE;
                    val animator: ValueAnimator = HYTAnimationUtil.getAnimator {
                        val value: Float = it.animatedValue as Float
                        _modal.alpha = value;
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
        _visualizerModel.longClick.value = {
            startActivityIfNeeded(Intent(this, HYTLibrary::class.java), 100);
        }
        _visualizerModel.data.value = HYTVisualizerModel.Companion.HYTRendererParameters(
            HYTUtil.readSource(resources.getString(R.string.vertex_shader), assets),
            HYTUtil.readSource(resources.getString(R.string.fragment_shader), assets),
            states
        );
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