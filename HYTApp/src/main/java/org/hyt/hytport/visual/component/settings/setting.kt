package org.hyt.hytport.visual.component.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hyt.hytport.R
import org.hyt.hytport.visual.api.model.HYTSettingState
import org.hyt.hytport.visual.component.montserrat

@Composable
fun setting(
    state: HYTSettingState,
    modifier: Modifier = Modifier
): Unit {
    val toggle: Boolean = state.state();
    val animate: Float by animateFloatAsState(
        targetValue = if (toggle) 1.0f else 0.0f
    );
    val color: Color by animateColorAsState(
        targetValue = if (toggle)
            colorResource(R.color.hyt_accent)
        else
            colorResource(R.color.hyt_accent_dark)
    )
    Row(
       verticalAlignment = Alignment.CenterVertically,
       modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .weight(1.0f)
        ) {
            Text(
                text = state.title(),
                color = colorResource(R.color.hyt_grey),
                fontFamily = montserrat,
                fontSize = 25.sp
            );
            Text(
                text = state.description(),
                color = colorResource(R.color.hyt_text_dark),
                fontFamily = montserrat,
            );
        }
        Box(
            modifier = Modifier
                .requiredSize(
                    size = 30.dp
                )
                .aspectRatio(
                    ratio = 1.0f
                )
                .clickable {
                    state.toggle();
                }
                .drawBehind {
                    val width: Float = size.width;
                    val diameter: Float = width * 0.4f;
                    val stroke: Float = diameter * 0.1f;
                    val start: Float = width * 0.1f;
                    val end: Float = width - 2.0f * start;
                    val circleCenter: Float = start + animate * end;
                    drawLine(
                        color = color,
                        start = Offset(
                            x = start - stroke * 0.5f,
                            y = center.y
                        ),
                        end = Offset(
                            x = (circleCenter - (diameter + stroke * 0.5f))
                                .coerceIn(start..end),
                            y = center.y
                        ),
                        strokeWidth = stroke,
                        cap = StrokeCap.Round
                    );
                    drawCircle(
                        color = color,
                        radius = diameter * 0.5f,
                        center = Offset(
                            x = circleCenter,
                            y = center.y
                        )
                    );
                    drawLine(
                        color = color,
                        start = Offset(
                            x = (circleCenter + diameter + stroke * 0.5f)
                                .coerceIn(start..end),
                            y = center.y
                        ),
                        end = Offset(
                            x = start + end + stroke * 0.5f,
                            y = center.y
                        ),
                        strokeWidth = stroke,
                        cap = StrokeCap.Round
                    );
                }
        ) {
        }
    }
}