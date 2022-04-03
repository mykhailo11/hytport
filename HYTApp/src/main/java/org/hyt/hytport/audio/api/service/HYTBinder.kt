package org.hyt.hytport.audio.api.service

import android.os.IBinder

interface HYTBinder: HYTAudioPlayer, IBinder {

    companion object {

        public interface HYTAuditor: HYTAudioPlayer.Companion.HYTAuditor {

            fun onSetPlayer(player: HYTAudioPlayer): Unit {}

            fun consumer(food: ByteArray): Unit{}

        }

    }

    fun addAuditor(auditor: HYTAuditor): Unit;

    fun removeAuditor(auditor: HYTAuditor): Unit;

    fun setPlayer(player: HYTAudioPlayer): Unit;

    fun getPlayer(consumer: (player: HYTAudioPlayer?) -> Unit): Unit;

}