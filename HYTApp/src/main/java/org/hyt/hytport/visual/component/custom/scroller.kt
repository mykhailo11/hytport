package org.hyt.hytport.visual.component.custom

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import org.hyt.hytport.R
import java.util.concurrent.ScheduledFuture
import kotlin.math.roundToInt

@Composable
fun scroller(
    stateController: ((Float) -> Unit) -> Unit,
    scrollConsumer: (Float) -> Unit
) {
    var scrollerHeight: Int by remember { mutableStateOf(0) };
    var barHeight: Int by remember { mutableStateOf(0) };
    val finish: Int by remember(scrollerHeight, barHeight) {
        derivedStateOf {
            barHeight - scrollerHeight;
        };
    }
    var offset: Int by remember { mutableStateOf(0) };
    var scheduled: ScheduledFuture<*>? by remember { mutableStateOf(null) };
    stateController { scrolled: Float ->
        offset = (scrolled * finish).roundToInt();
        if (offset < 0) {
            offset = 0;
        }
        if (offset > finish) {
            offset = finish;
        }
    }
    val scroller: ScrollableState = rememberScrollableState { delta: Float ->
        val pixels: Int = delta.roundToInt();
        if (offset + pixels in 0..finish) {
            offset += pixels;
            scrollConsumer(offset.toFloat() / finish.toFloat());
        }
        if (offset < 0) {
            offset = 0;
        }
        if (offset > finish) {
            offset = finish;
        }
        delta;
    }
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .padding(10.dp)
            .onGloballyPositioned { coordinates: LayoutCoordinates ->
                barHeight = coordinates.size.height
            }
    ) {
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(0, offset);
                }
                .requiredWidth(3.dp)
                .requiredHeight(80.dp)
                .background(
                    color = colorResource(R.color.hyt_grey),
                    shape = RoundedCornerShape(50)
                )
                .onGloballyPositioned { coordinates: LayoutCoordinates ->
                    scrollerHeight = coordinates.size.height;
                }
                .scrollable(
                    state = scroller,
                    orientation = Orientation.Vertical
                )
        );
    }
}