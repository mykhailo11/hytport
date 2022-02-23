package org.hyt.hytport.visual.factory

import android.content.Context
import android.widget.Adapter
import android.widget.ListAdapter
import org.hyt.hytport.audio.api.model.HYTAudioModel
import org.hyt.hytport.visual.model.HYTAudioModelAdapter
import java.util.*

class HYTViewFactory {

    companion object {
        public fun getAudioAdapter(context: Context, queue: Deque<HYTAudioModel>, click: (HYTAudioModel) -> Unit): ListAdapter{
            return HYTAudioModelAdapter(context, queue, click);
        }
    }

}