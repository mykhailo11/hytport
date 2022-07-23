package org.hyt.hytport.visual.component.custom

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import org.hyt.hytport.R
import org.hyt.hytport.util.HYTUtil
import org.hyt.hytport.visual.api.model.HYTPlayerScrollState
import kotlin.math.roundToInt

@Composable
fun slider(
    state: HYTPlayerScrollState,
    showTime: Boolean = false,
    seek: (to: Int) -> Unit,
    modifier: Modifier = Modifier
): Unit {
    val animation: Float by animateFloatAsState(
        if (showTime) 1.0f
        else 0.0f
    );
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Text(
            text = HYTUtil.formatTime(state.slider()),
            color = colorResource(R.color.hyt_white),
            modifier = Modifier
                .alpha(animation)
        );
        Slider(
            value = state.slider(),
            enabled = true,
            onValueChange = { value: Float ->
                if (!state.sliding()) {
                    state.sliding(true);
                }
                state.slider(value);
            },
            onValueChangeFinished = {
                state.sliding(false);
                seek((state.slider() * 1000.0f).roundToInt())
            },
            colors = SliderDefaults.colors(
                thumbColor = colorResource(R.color.hyt_grey),
                activeTrackColor = colorResource(R.color.hyt_grey)
            ),
            valueRange = 0.0f..state.max(),
            modifier = Modifier
                .requiredHeight(40.dp)
                .weight(
                    weight = 1.0f
                )
                .graphicsLayer(
                    scaleX = 0.8f,
                    scaleY = 0.8f
                )
        );
        Text(
            text = HYTUtil.formatTime(state.max()),
            color = colorResource(R.color.hyt_white),
            modifier = Modifier
                .alpha(animation)
        );
    }
}