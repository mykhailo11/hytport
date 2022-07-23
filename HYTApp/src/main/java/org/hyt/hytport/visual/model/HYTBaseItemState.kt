package org.hyt.hytport.visual.model

import android.content.Context
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import org.hyt.hytport.audio.api.model.HYTAudioModel
import org.hyt.hytport.util.HYTUtil
import org.hyt.hytport.visual.api.model.HYTItemState

class HYTBaseItemState public constructor(
    context: Context,
    item: HYTAudioModel,
    empty: Painter,

) : HYTItemState {

    private val _auditors: MutableList<HYTItemState.Companion.HYTAuditor> = ArrayList();

    private var _item: HYTAudioModel by mutableStateOf(item);

    private var _current: Boolean by mutableStateOf(false);

    private var _cover: ImageBitmap? by mutableStateOf(null);

    private val _empty: Painter = empty;

    private val _context: Context = context;

    override fun item(): HYTAudioModel {
        return _item;
    }

    override fun item(item: HYTAudioModel) {
        _cover = HYTUtil.getBitmap(item.getAlbumPath(), _context.contentResolver)
            ?.asImageBitmap();
        _auditors.forEach { auditor: HYTItemState.Companion.HYTAuditor ->
            auditor.onItemSet(item);
        }
        _item = item;
    }

    override fun cover(modifier: Modifier): @Composable () -> Unit {
        return if (_cover == null) {
            {
                Image(
                    painter = _empty,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = modifier
                );
            }
        } else {
            {
                Image(
                    bitmap = _cover!!,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = modifier
                );
            }
        };
    }

    override fun current(): Boolean {
        return _current;
    }

    override fun current(current: Boolean) {
        _current = current;
    }

    override fun click() {
        TODO("Not yet implemented")
    }

    override fun select() {
        TODO("Not yet implemented")
    }

    override fun background(): Color {
        TODO("Not yet implemented")
    }

    override fun addAuditor(auditor: HYTItemState.Companion.HYTAuditor) {
        TODO("Not yet implemented")
    }

    override fun removeAuditor(auditor: HYTItemState.Companion.HYTAuditor) {
        TODO("Not yet implemented")
    }

}