package org.hyt.hytport.visual.fragment.visualizer

import android.view.View
import androidx.lifecycle.ViewModel
import org.hyt.hytport.visual.api.model.HYTState

class HYTVisualizerModel: ViewModel() {

    private var _onSurfaceClick: (View) -> Unit = {};

    private var _onSurfaceLongCLick: (View) -> Boolean = {true};

    public var vertexShader: String? = null;

    public var fragmentShader: String? = null;

    public var states: Map<String, Array<HYTState>>? = null;

    public fun setSurfaceClick(trigger: (View) -> Unit) {
        _onSurfaceClick = trigger;
    }

    public fun setSurfaceLongClick(trigger: (View) -> Boolean) {
        _onSurfaceLongCLick = trigger;
    }

    public fun surfaceClick(view: View): Unit {
        _onSurfaceClick(view);
    }

    public fun surfaceLongClick(view: View): Boolean {
        return _onSurfaceLongCLick(view);
    }

}