package org.hyt.hytport.visual.fragment.visualizer

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import org.hyt.hytport.R
import org.hyt.hytport.graphics.factory.HYTGLFactory

class HYTVisualizer: Fragment(R.layout.hyt_visualizer) {

    private var _surface: GLSurfaceView? = null;

    private val _model: HYTVisualizerModel by activityViewModels();

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _surface = view.findViewById(R.id.hyt_visualizer);
        if (
            _surface != null
            && _model.vertexShader != null
            && _model.fragmentShader != null
            && _model.states != null
        ){
            val surface: GLSurfaceView = _surface!!;
            surface.setOnClickListener {
                _model.surfaceClick(it);
            }
            surface.setOnLongClickListener {
                _model.surfaceLongClick(it);
            }
            surface.setEGLContextClientVersion(3);
            surface.setRenderer(
                HYTGLFactory.getCanvas(
                    view.context,
                    _model.vertexShader!!,
                    _model.fragmentShader!!,
                    _model.states!!
                )
            );
        }
    }

}