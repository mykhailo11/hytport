package org.hyt.hytport.visual.fragment.visualizer

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import org.hyt.hytport.R
import org.hyt.hytport.graphics.factory.HYTGLFactory

class HYTVisualizer : Fragment(R.layout.hyt_visualizer_fragment) {

    private var _surface: GLSurfaceView? = null;

    private val _model: HYTVisualizerModel by activityViewModels();

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _surface = view.findViewById(R.id.hyt_visualizer);
        if (_surface != null) {
            _surface!!.setEGLContextClientVersion(3);
            _model.data.observe(this) {
                _surface!!.setRenderer(
                    HYTGLFactory.getCanvas(
                        view.context,
                        it.vertexShader,
                        it.fragmentShader,
                        it.states
                    )
                );
            };
            _model.click.observe(this) {
                _surface!!.setOnClickListener {
                    it();
                };
            }
            _model.longClick.observe(this) {
                _surface!!.setOnLongClickListener {
                    it();
                }
            }
        }
    }

    override fun onPause() {
        _surface!!.onPause();
        super.onPause()
    }

    override fun onResume() {
        _surface!!.onResume();
        super.onResume();
    }

}