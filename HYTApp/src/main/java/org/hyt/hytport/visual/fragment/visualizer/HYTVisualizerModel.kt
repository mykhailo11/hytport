package org.hyt.hytport.visual.fragment.visualizer

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.hyt.hytport.visual.api.model.HYTState

class HYTVisualizerModel : ViewModel() {

    companion object {

        public class HYTRendererParameters public constructor(
            vertexShader: String,
            fragmentShader: String,
            states: Map<String, Array<HYTState>>
        ){

            val vertexShader: String;

            val fragmentShader: String;

            public val states: Map<String, Array<HYTState>>;

            init {
                this.vertexShader = vertexShader;
                this.fragmentShader = fragmentShader;
                this.states = states;
            }

        }

    }

    public val data: MutableLiveData<HYTRendererParameters> by lazy {
        MutableLiveData();
    }

    public val click: MutableLiveData<() -> Unit> by lazy {
        MutableLiveData();
    }

    public val longClick: MutableLiveData<() -> Boolean> by lazy {
        MutableLiveData();
    }

}