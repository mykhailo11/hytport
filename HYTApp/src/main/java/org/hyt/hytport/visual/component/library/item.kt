package org.hyt.hytport.visual.component.library

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hyt.hytport.R
import org.hyt.hytport.audio.api.model.HYTAudioModel
import org.hyt.hytport.util.HYTUtil

@Composable
fun item(
    item: HYTAudioModel,
    current: Boolean,
    click: (HYTAudioModel) -> Unit
) {
    val context: Context = LocalContext.current;
    val title: String? by produceState(
        initialValue = null as String?,
        item
    ) {
        value = item.getTitle();
    };
    val titleScroll = rememberScrollState();
    val artist: String? by produceState(
        initialValue = null as String?,
        item
    ) {
        value = item.getArtist();
    };
    val artistScroll = rememberScrollState();
    val cover: Uri? by produceState(
        initialValue = null as Uri?,
        item
    ) {
        value = item.getAlbumPath();
    }
    val coverBitmap: ImageBitmap? by derivedStateOf {
        HYTUtil.getBitmap(cover, context.contentResolver)?.asImageBitmap()
    };
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(20.dp)
            .clickable {
                click(item);
            }
    ){
        if (coverBitmap == null) {
            Image(
                painter = painterResource(R.drawable.hyt_empty_cover_200dp),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .requiredHeight(50.dp)
                    .aspectRatio(1.0f)
            );
        }else {
            Image(
                bitmap = coverBitmap!!,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .requiredHeight(50.dp)
                    .aspectRatio(1.0f)
            )
        };
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 10.dp
                )
        ) {
            Text(
                text = if (title == null) "unknown" else title!!,
                color = if (current) colorResource(R.color.hyt_press) else colorResource(R.color.hyt_white),
                fontSize = 20.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(
                        state = titleScroll
                    )
            );
            Text(
                text = if (artist == null) "unknown" else artist!!,
                color = if (current) colorResource(R.color.hyt_press) else colorResource(R.color.hyt_text_dark),
                fontSize = 16.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(
                        state = artistScroll
                    )
            );
        }
    }
}
