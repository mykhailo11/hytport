package org.hyt.hytport.visual.fragment.player

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.hyt.hytport.audio.api.service.HYTBinder

class HYTPlayerModel: ViewModel() {

    val player: MutableLiveData<HYTBinder> by lazy {
        MutableLiveData();
    }

    var start: () -> Unit = {};

}