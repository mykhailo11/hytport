package org.hyt.hytport.visual.component.library

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hyt.hytport.R
import org.hyt.hytport.audio.api.model.HYTAudioModel
import org.hyt.hytport.util.HYTUtil
import org.hyt.hytport.visual.api.model.HYTItemController
import org.hyt.hytport.visual.component.montserrat
import org.hyt.hytport.visual.component.util.pressed
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

@Composable
fun item(
    controller: HYTItemController,
    executor: ScheduledExecutorService,
    empty: Painter,
    item: HYTAudioModel,
    current: Boolean,
    click: (HYTAudioModel) -> Unit,
    selectedBackground: Color = colorResource(R.color.hyt_grey),
    modifier: Modifier = Modifier
) {
    var focused: Boolean by remember { mutableStateOf(false) };
    var selected: Boolean by remember { mutableStateOf(false) };
    val selectedEffect: Boolean by rememberUpdatedState(selected);
    val focusedEffect: Boolean by rememberUpdatedState(focused);
    val itemEffect: HYTAudioModel by rememberUpdatedState(item);
    DisposableEffect(controller) {
        val auditor: HYTItemController.Companion.HYTAuditor = object :
            HYTItemController.Companion.HYTAuditor {

            override fun onFocus(audio: HYTAudioModel?, move: Boolean): Boolean {
                val selectedItem: HYTAudioModel? = controller.selected();
                val key: Long = itemEffect.getId();
                if (
                    selectedItem?.getId() == key
                    && audio?.getId() == key
                    && move
                    && !selectedEffect
                ) {
                    selected = true;
                }
                if (
                    audio?.getId() == key
                    && move
                    && !focusedEffect
                ) {
                    focused = true;
                }
                if (audio == null && selectedEffect) {
                    selected = false;
                }
                if (audio == null && focusedEffect) {
                    focused = false;
                }
                return super.onFocus(audio, move);
            }

        };
        controller.addAuditor(auditor);
        onDispose {
            controller.removeAuditor(auditor);
        }
    }
    val color: Color by animateColorAsState(
        when {
            selected && focused -> selectedBackground
            focused -> colorResource(R.color.hyt_accent_grey)
            else -> colorResource(R.color.hyt_transparent)
        }
    );
    val context: Context = LocalContext.current;
    var title: String by remember { mutableStateOf("loading..") };
    val titleScroll = rememberScrollState();
    var artist: String by remember { mutableStateOf("loading..") };
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
    val coverModifier: Modifier = Modifier
        .requiredHeight(50.dp)
        .aspectRatio(1.0f)
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = pressed(
                    pressed = {
                        controller.select(item);
                    },
                    react = {
                        if (controller.focused() == null) {
                            controller.select(null);
                        }
                    }
                )
            )
        }
    Row(
        modifier = Modifier
            .background(
                color = color,
                shape = remember { RoundedCornerShape(30) }
            )
            .then(modifier)
            .wrapContentHeight()
            .animateContentSize()
            .pointerInput(Unit) {
                detectTapGestures {
                    click(item);
                }
            }
            .wrapContentWidth()
            .padding(
                horizontal = 8.dp,
                vertical = 15.dp
            )
    ) {
        if (cover == null) {
            Image(
                painter = empty,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = coverModifier
            );
        } else {
            Image(
                bitmap = cover!!,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = coverModifier
            )
        };
        if (!selected) {
            Column(
                modifier = Modifier
                    .padding(
                        start = 10.dp
                    )
            ) {
                Text(
                    text = title,
                    fontFamily = montserrat,
                    fontWeight = FontWeight.Light,
                    color = if (current) colorResource(R.color.hyt_press) else colorResource(R.color.hyt_white),
                    fontSize = 20.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(
                            state = titleScroll
                        )
                );
                Text(
                    text = artist,
                    fontFamily = montserrat,
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
}
