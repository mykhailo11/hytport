package org.hyt.hytport.visual.component.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import org.hyt.hytport.R
import org.hyt.hytport.util.HYTUtil
import org.hyt.hytport.visual.component.fonts
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

@Composable
fun search(
    executor: ScheduledExecutorService,
    search: (Regex) -> Unit,
    reset: () -> Unit
) {
    var filter: String by remember { mutableStateOf("") };
    DisposableEffect(
        filter,
        executor,
        search,
        reset,
        effect = {
            val scheduled: ScheduledFuture<*> = executor.schedule(
                {
                    if (filter.isNotBlank() && filter.matches("[A-Za-z0-9]*".toRegex())) {
                        search(HYTUtil.anyMatch(filter).toRegex());
                    } else {
                        reset();
                    }
                }, 500, TimeUnit.MILLISECONDS
            );
            onDispose {
                scheduled.cancel(true);
            }
        }
    )
    Text(
        text = remember { "Search" },
        fontFamily = fonts,
        color = colorResource(R.color.hyt_black),
        modifier = Modifier
            .background(
                color = colorResource(R.color.hyt_accent),
                shape = remember {
                    RoundedCornerShape(30, 0, 0, 30)
                }
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
                shape = remember {
                    RoundedCornerShape(0, 30, 30, 0)
                }
            )
            .padding(15.dp)
    );
}