package org.hyt.hytport.visual.api.model

import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.hyt.hytport.audio.api.model.HYTAudioModel

interface HYTItemState {

    companion object {

         interface HYTAuditor {

             fun onClick(audio: HYTAudioModel): Unit {};

             fun onItemSet(audio: HYTAudioModel): Unit {};

        }

    }

    fun item(): HYTAudioModel;

    fun item(item: HYTAudioModel): Unit;

    fun cover(modifier: Modifier): @Composable () -> Unit;

    fun current(): Boolean;

    fun current(current: Boolean): Unit;

    fun click(): Unit;

    fun select(): Unit;

    fun background(): Color

    fun addAuditor(auditor: HYTAuditor): Unit;

    fun removeAuditor(auditor: HYTAuditor): Unit;

}