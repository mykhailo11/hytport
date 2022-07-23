package org.hyt.hytport.visual.component.custom

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import org.hyt.hytport.R
import org.hyt.hytport.visual.api.model.HYTScrollerState

@Composable
fun scroller(
    scrollerState: HYTScrollerState,
    modifier: Modifier = Modifier
) {
    val density: Density = LocalDensity.current;
    val scrollerColor: Color by animateColorAsState(
        if (scrollerState.fastScroll())
            colorResource(R.color.hyt_grey)
        else
            colorResource(R.color.hyt_accent_grey)
    );
    val height: Float by animateFloatAsState(
        with(density) {
            scrollerState.barHeight().toDp().value;
        } * scrollerState.scrollerScale()
    );
    val back: Color = colorResource(R.color.hyt_scroller);
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .then(modifier)
            .drawBehind {
                val stroke: Float = size.width * 0.5f;
                drawLine(
                    color = back,
                    start = Offset(
                        x = center.x,
                        y = stroke
                    ),
                    end = Offset(
                        x = center.x,
                        y = size.height - stroke
                    ),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round
                )
            }
            .clip(
                shape = remember { RoundedCornerShape(50) }
            )
            .onGloballyPositioned { coordinates: LayoutCoordinates ->
                scrollerState.barHeight(coordinates.size.height);
            }
    ) {
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(0, scrollerState.offset());
                }
                .requiredWidth(5.dp)
                .requiredHeight((height).dp)
                .background(
                    color = scrollerColor,
                    shape = remember { RoundedCornerShape(50) }
                )
                .scrollable(
                    state = scrollerState.scroller() ?: rememberScrollableState { it },
                    orientation = Orientation.Vertical
                )
                .onGloballyPositioned { coordinates: LayoutCoordinates ->
                    scrollerState.scrollerHeight(coordinates.size.height);
                }
        );
    }
}
