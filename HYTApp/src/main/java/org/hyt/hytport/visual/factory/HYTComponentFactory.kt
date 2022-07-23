package org.hyt.hytport.visual.factory

import android.content.Context
import androidx.compose.foundation.ScrollState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.AnnotatedString
import androidx.recyclerview.widget.RecyclerView
import org.hyt.hytport.audio.api.model.HYTAudioModel
import org.hyt.hytport.visual.api.model.*
import org.hyt.hytport.visual.model.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.ScheduledExecutorService

class HYTComponentFactory {

    companion object {

        fun getControlState(
            playing: Boolean = false,
            next: Painter,
            nextActive: Painter,
            previous: Painter,
            previousActive: Painter,
            play: Painter,
            playActive: Painter,
            playPress: Painter
        ): HYTControlState {
            return HYTBaseControlState(
                playing = playing,
                next = next,
                nextActive = nextActive,
                previous = previous,
                previousActive = previousActive,
                play = play,
                playActive = playActive,
                playPress = playPress
            );
        }

        fun getPlayerState(
            artistDefault: String = "Artist",
            titleDefault: String = "Title"
        ): HYTPlayerState {
            return HYTBasePlayerState(
                artistDefault = artistDefault,
                titleDefault = titleDefault
            );
        }

        fun getPlayerScrollState(
            slider: Float,
            max: Float,
            artistScroll: ScrollState,
            titleScroll: ScrollState
        ): HYTPlayerScrollState {
            return HYTBasePlayerScrollState(
                slider = slider,
                max = max,
                artistScroll = artistScroll,
                titleScroll = titleScroll
            );
        }

        fun getScrollerState(
            initialOffset: Int = 0,
            recycler: RecyclerView?,
            executor: ScheduledExecutorService
        ): HYTScrollerState {
            return HYTBaseScrollerState(
                initialOffset = initialOffset,
                recycler = recycler,
                executor = executor
            )
        }

        fun getItemController(
            executor: ExecutorService
        ): HYTItemController {
            return HYTBaseItemController(
                executor = executor
            );
        }

        fun getEditorState(
            initialValue: String,
            label: String
        ): HYTEditorState {
            return HYTBaseEditorState(initialValue, label);
        }

        fun getFormState(
            initialTitle: String,
            initialConfirm: String
        ): HYTFormState {
            return HYTBaseFormState(initialTitle, initialConfirm);
        }

        fun getPopState(
            initialTitle: String,
            initialContent: AnnotatedString,
            initialConfirm: String,
            initialPainter: Painter?
        ): HYTPopState {
            return HYTBasePopState(
                initialTitle = initialTitle,
                initialContent = initialContent,
                initialConfirm = initialConfirm,
                initialPainter = initialPainter
            );
        }

        fun getClipState(): HYTClipState {
            return HYTBaseClipState();
        }

        fun getQueueController(
            currentBack: Color,
            hoverBack: Color,
            back: Color,
            text: Color,
            currentText: Color,
            executor: ExecutorService
        ): HYTQueueController {
            return HYTBaseQueueController(
                currentBack = currentBack,
                hoverBack = hoverBack,
                back = back,
                text = text,
                currentText = currentText,
                executor = executor
            );
        }

        fun getHoverState(): HYTHoverState {
            return HYTBaseHoverState();
        }

        fun getItemState(
            context: Context,
            item: HYTAudioModel,
            empty: Painter
        ): HYTItemState {
            return HYTBaseItemState(
                context = context,
                item = item,
                empty = empty
            );
        }

        fun getSettingState(
            initial: Boolean,
            initialTitle: String,
            initialDescription: String
        ): HYTSettingState {
            return HYTBaseSettingState(
                initial = initial,
                initialTitle = initialTitle,
                initialDescription = initialDescription
            );
        }

    }

}