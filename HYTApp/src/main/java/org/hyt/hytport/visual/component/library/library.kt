package org.hyt.hytport.visual.component.library

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hyt.hytport.R
import org.hyt.hytport.audio.api.model.HYTAudioModel
import org.hyt.hytport.util.HYTUtil

@Composable
fun library(
    libraryItems: List<HYTAudioModel>?,
    current: Long,
    click: (HYTAudioModel) -> Unit,
    back: () -> Unit
) {
    var filter: String by remember { mutableStateOf("") };
    val filtered: List<HYTAudioModel> by produceState(
        initialValue = emptyList(),
        filter,
        libraryItems
    ) {
        if (libraryItems != null && filter.isNotBlank() && filter.matches("[A-Za-z0-9]*".toRegex())) {
            value = libraryItems.filter { audio: HYTAudioModel ->
                matches(audio, HYTUtil.anyMatch(filter).toRegex());
            }
        } else if (libraryItems != null) {
            value = libraryItems;
        }
    }
    val columnState = rememberLazyListState();
    Column(
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier
            .fillMaxSize()
    ) {
        if (filtered.isNotEmpty()) {
            LazyColumn(
                state = columnState,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1.0f)
                    .padding(
                        start = 10.dp,
                        end = 10.dp,
                        top = 10.dp,
                        bottom = 0.dp
                    )
            ) {
                items(
                    items = filtered,
                    key = { audio: HYTAudioModel ->
                        audio.getId();
                    }
                ) { audio: HYTAudioModel ->
                    item(audio, audio.getId() == current, click);
                }
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ){
            Image(
                painter = painterResource(R.drawable.hyt_library_open_200dp),
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
                text = "Search",
                color = colorResource(R.color.hyt_grey),
                fontSize = 18.sp,
                modifier = Modifier
                    .background(
                        color = colorResource(R.color.hyt_accent),
                        shape = RoundedCornerShape(15, 0, 0, 15)
                    )
                    .clip(RoundedCornerShape(15, 0, 0, 15))
                    .padding(15.dp)
            );
            BasicTextField(
                value = filter,
                onValueChange = { new: String ->
                    filter = new;
                },
                textStyle = TextStyle(
                    color = colorResource(R.color.hyt_text_dark),
                    fontSize = 18.sp
                ),
                cursorBrush = SolidColor(colorResource(R.color.hyt_text_dark)),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(0, 15, 15, 0))
                    .border(
                        width = 2.dp,
                        color = colorResource(R.color.hyt_accent),
                        shape = RoundedCornerShape(0, 15, 15, 0)
                    )
                    .padding(15.dp)
            );
        }
    }
}

fun matches(audio: HYTAudioModel, filter: Regex): Boolean {
    val title: String? = audio.getTitle();
    val artist: String? = audio.getArtist();
    return (title != null && title.matches(filter))
            || (artist != null && artist.matches(filter));
}