package org.hyt.hytport.visual.component.library

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.hyt.hytport.R
import org.hyt.hytport.audio.api.model.HYTAudioModel
import org.hyt.hytport.util.HYTUtil
import org.hyt.hytport.visual.component.custom.scroller
import org.hyt.hytport.visual.component.fonts
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

@Composable
fun library(
    libraryItems: List<HYTAudioModel>?,
    current: Long,
    click: (HYTAudioModel) -> Unit,
    back: () -> Unit,
    executor: ScheduledExecutorService
) {
    var filter: String by remember { mutableStateOf("") };
    var filtered: List<HYTAudioModel> by remember { mutableStateOf(emptyList()) };
    DisposableEffect(
        filter,
        libraryItems
    ) {
        val scheduled: ScheduledFuture<*> = executor.schedule(
            {
                if (libraryItems != null && filter.isNotBlank() && filter.matches("[A-Za-z0-9]*".toRegex())) {
                    filtered = libraryItems.filter { audio: HYTAudioModel ->
                        matches(audio, HYTUtil.anyMatch(filter).toRegex());
                    }
                } else if (libraryItems != null) {
                    filtered = libraryItems;
                }
            }, 500, TimeUnit.MILLISECONDS
        );
        onDispose {
            scheduled.cancel(true);
        }
    }
    val columnState = rememberLazyListState();
    var scrollStateConsumer: ((Float) -> Unit)? by remember { mutableStateOf(null) };
    val scrollState = rememberScrollableState { delta: Float ->
        if (scrollStateConsumer != null) {
            scrollStateConsumer!!(
                columnState.firstVisibleItemIndex
                        / columnState.layoutInfo.totalItemsCount.toFloat()
            );
        }
        delta
    }
    val scrollProcess = rememberCoroutineScope();
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.hyt_library_close_200dp),
                contentDescription = null,
                modifier = Modifier
                    .height(30.dp)
                    .aspectRatio(1.0f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        back()
                    }
                    .padding(
                        start = 0.dp,
                        top = 0.dp,
                        end = 10.dp,
                        bottom = 0.dp
                    )
            )
            Text(
                text = remember { "Search" },
                fontFamily = fonts,
                color = colorResource(R.color.hyt_black),
                modifier = Modifier
                    .background(
                        color = colorResource(R.color.hyt_accent),
                        shape = RoundedCornerShape(30, 0, 0, 30)
                    )
                    .padding(15.dp)
            );
            BasicTextField(
                value = filter,
                onValueChange = { new: String ->
                    filter = new;
                },
                textStyle = TextStyle(
                    color = colorResource(R.color.hyt_text_dark),
                    fontFamily = fonts,
                ),
                cursorBrush = SolidColor(colorResource(R.color.hyt_text_dark)),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 2.dp,
                        color = colorResource(R.color.hyt_accent),
                        shape = RoundedCornerShape(0, 30, 30, 0)
                    )
                    .padding(15.dp)
            );
        }
        if (filtered.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .weight(1.0f)
                    .padding(
                        start = 10.dp,
                        end = 10.dp,
                        top = 0.dp,
                        bottom = 10.dp
                    )
            ) {
                LazyColumn(
                    state = columnState,
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1.0f)
                        .scrollable(
                            state = scrollState,
                            orientation = Orientation.Vertical
                        )
                ) {
                    items(
                        items = filtered,
                        key = { item: HYTAudioModel ->
                            item.getId()
                        }
                    ) { audio: HYTAudioModel ->
                        item(
                            item = audio,
                            empty = painterResource(R.drawable.hyt_empty_cover_200dp),
                            current = audio.getId() == current,
                            click = click,
                            executor = executor
                        );
                    }
                }
                scroller(
                    stateController = { scrollConsumer: (Float) -> Unit ->
                        scrollStateConsumer = scrollConsumer;
                    },
                    scrollConsumer = { scroll: Float ->
                        scrollProcess.launch {
                            columnState.scrollToItem(
                                (columnState.layoutInfo.totalItemsCount * scroll).roundToInt()
                            );
                        }
                    }
                );
            }
        }
    }
}

fun matches(audio: HYTAudioModel, filter: Regex): Boolean {
    val title: String? = audio.getTitle();
    val artist: String? = audio.getArtist();
    return (title != null && title.matches(filter))
            || (artist != null && artist.matches(filter));
}