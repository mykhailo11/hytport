package org.hyt.hytport.visual.component.library

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hyt.hytport.R
import org.hyt.hytport.audio.api.model.HYTAudioModel
import org.hyt.hytport.util.HYTUtil
import org.hyt.hytport.visual.component.fonts
import org.hyt.hytport.visual.component.util.pressed
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

@Composable
fun item(
    executor: ScheduledExecutorService,
    empty: Painter,
    item: HYTAudioModel,
    current: Boolean,
    click: (HYTAudioModel) -> Unit,
    focus: ((Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context: Context = LocalContext.current;
    var title: String by remember { mutableStateOf("loading...") };
    val titleScroll = rememberScrollState();
    var artist: String by remember { mutableStateOf("loading...") };
    val artistScroll = rememberScrollState();
    var cover: ImageBitmap? by remember { mutableStateOf(null) };
    DisposableEffect(item) {
        val scheduled: ScheduledFuture<*> = executor.schedule({
            val bitmap: Bitmap? = HYTUtil.getBitmap(item.getAlbumPath(), context.contentResolver)
            if (bitmap != null) {
                cover = bitmap.asImageBitmap();
            }
            title = item.getTitle() ?: "unknown";
            artist = item.getArtist() ?: "unknown";
        }, 200, TimeUnit.MILLISECONDS);
        onDispose {
            scheduled.cancel(true);
        }
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(
                horizontal = 10.dp,
                vertical = 0.dp
            )
            .then(modifier)
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(
                horizontal = 10.dp,
                vertical = 20.dp
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                click(item);
            }
    ) {
        if (cover == null) {
            Image(
                painter = empty,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .requiredHeight(50.dp)
                    .aspectRatio(1.0f)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = pressed(
                                pressed = {
                                    focus?.invoke(true);
                                },
                                react = { up: Boolean ->
                                    focus?.invoke(!up);
                                }
                            )
                        )
                    }
            );
        } else {
            Image(
                bitmap = cover!!,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .requiredHeight(50.dp)
                    .aspectRatio(1.0f)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = pressed(
                                pressed = {
                                    focus?.invoke(true);
                                },
                                react = { up: Boolean ->
                                    focus?.invoke(!up);
                                }
                            )
                        )
                    }
            )
        };
        Column(
            modifier = Modifier
                .weight(1.0f)
                .padding(
                    start = 10.dp
                )
        ) {
            Text(
                text = if (title == null) "unknown" else title!!,
                fontFamily = fonts,
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
                fontFamily = fonts,
                color = if (current) colorResource(R.color.hyt_press) else colorResource(R.color.hyt_text_dark),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(
                        state = artistScroll
                    )
            );
        }
    }
}
