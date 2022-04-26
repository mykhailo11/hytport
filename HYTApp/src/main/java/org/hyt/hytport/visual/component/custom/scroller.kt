package org.hyt.hytport.visual.component.custom

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.hyt.hytport.R
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

private class ScrollerState {

    public var offset: Int by mutableStateOf(0);

    public var scheduledScroll: ScheduledFuture<*>? = null;

}

@Composable
fun scroller(
    executor: ScheduledExecutorService,
    recycler: RecyclerView
) {
    val layout: LinearLayoutManager by remember(recycler) {
        derivedStateOf {
            recycler.layoutManager as LinearLayoutManager
        }
    };
    val threshold: Float by remember(layout) {
        derivedStateOf {
            10.0f * (layout.findLastVisibleItemPosition()
                    - layout.findFirstVisibleItemPosition()).toFloat() / layout.itemCount.toFloat();
        }
    }
    var fastScroll: Boolean by remember { mutableStateOf(false) };
    var scrollerHeight: Int by remember { mutableStateOf(0) };
    var barHeight: Int by remember { mutableStateOf(0) };
    val finish: Int by remember(scrollerHeight, barHeight) {
        derivedStateOf {
            barHeight - scrollerHeight;
        };
    }
    val scrollerColor: Color by animateColorAsState(
        if (fastScroll)
            colorResource(R.color.hyt_accent)
        else
            colorResource(R.color.hyt_grey)
    );
    val scrollerState: ScrollerState by remember {
        derivedStateOf {
            ScrollerState()
        }
    };
    val scroller: ScrollableState = rememberScrollableState { delta: Float ->
        val pixels: Int = delta.roundToInt();
        val offset: Int = scrollerState.offset + pixels;
        val count: Int = layout.itemCount;
        val scroll: Int = (offset.toFloat() / finish * count).roundToInt();
        if (!fastScroll) {
            fastScroll = (delta / scrollerHeight).absoluteValue > threshold;
            recycler.scrollToPosition(
                if (scroll >= count) count - 1 else scroll
            );
        } else {
            scrollerState.scheduledScroll?.cancel(true);
            scrollerState.scheduledScroll = executor.schedule(
                {
                    fastScroll = false;
                    scrollerState.scheduledScroll = null;
                    recycler
                        .scrollToPosition(
                            if (scroll >= count) count - 1 else scroll
                        );
                },
                100,
                TimeUnit.MILLISECONDS
            )
        }
        if (offset < 0) {
            scrollerState.offset = 0;
        } else if (offset > finish) {
            scrollerState.offset = finish;
        } else {
            scrollerState.offset = offset;
        }
        delta;
    }
    DisposableEffect(
        fastScroll,
        finish,
        layout
    ) {
        val scroll: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!scroller.isScrollInProgress && !fastScroll) {
                    val first: Int = layout.findFirstCompletelyVisibleItemPosition();
                    val visible: Int = layout.findLastVisibleItemPosition() - first;
                    val offset: Int = (first.toFloat() / (layout.itemCount.toFloat()
                            - visible.toFloat()) * finish).roundToInt();
                    if (offset < 0) {
                        scrollerState.offset = 0;
                    } else if (offset > finish) {
                        scrollerState.offset = finish;
                    } else {
                        scrollerState.offset = offset;
                    }
                }
                super.onScrolled(recyclerView, dx, dy);
            }

        };
        recycler.addOnScrollListener(scroll);
        onDispose {
            recycler.removeOnScrollListener(scroll);
        }
    }
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .onGloballyPositioned { coordinates: LayoutCoordinates ->
                barHeight = coordinates.size.height
            }
            .padding(
                start = 0.dp,
                top = 0.dp,
                end = 10.dp,
                bottom = 0.dp
            )
    ) {
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(0, scrollerState.offset);
                }
                .requiredWidth(5.dp)
                .requiredHeight(60.dp)
                .background(
                    color = scrollerColor,
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