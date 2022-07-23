package org.hyt.hytport.visual.component.library

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import org.hyt.hytport.visual.api.model.HYTHoverState
import org.hyt.hytport.visual.api.model.HYTItemController
import org.hyt.hytport.visual.api.model.HYTQueueController
import org.hyt.hytport.visual.component.custom.hoverable
import org.hyt.hytport.visual.component.custom.primaryButton
import org.hyt.hytport.visual.component.custom.rememberHoverState

@Composable
fun queue(
    controller: HYTItemController,
    queueController: HYTQueueController,
    item: String
) {
    val queueControllerEffect: HYTQueueController by rememberUpdatedState(queueController);
    val itemEffect: String by rememberUpdatedState(item);
    val hoverState: HYTHoverState = rememberHoverState();
    val start: Boolean = queueController.consumer() == item;
    val animate: Float by animateFloatAsState(
        targetValue = if (start) 0.0f else 1.0f,
        animationSpec = tween(
            durationMillis = if (start) 400 else 0,
            delayMillis = if (start) 200 else 0
        ),
        finishedListener = {
            queueController.consume(null);
        }
    );
    DisposableEffect(hoverState) {
        val auditor: HYTHoverState.Companion.HYTAuditor = object:
            HYTHoverState.Companion.HYTAuditor {

            override fun onHover() {
                queueControllerEffect.hover(itemEffect);
            }

            override fun onOut() {
                queueControllerEffect.out(itemEffect);
            }

        }
        hoverState.addAuditor(auditor);
        onDispose {
            hoverState.removeAuditor(auditor);
        }
    }
    hoverable(
        state = hoverState,
        controller = controller,
        modifier = Modifier
            .drawBehind {
                val height: Float = size.height;
                drawCircle(
                    color = queueController.color(item),
                    radius = height * 0.15f,
                    center = Offset(
                        x = center.x,
                        y = center.y - height * animate
                    ),
                    alpha = if (start) 1.0f else 0.0f
                );
            }
    ) @Composable {
        primaryButton(
            text = item,
            textColor = queueController.text(item),
            color = queueController.color(item),
            round = 5,
            modifier = Modifier
                .padding(
                    horizontal = 25.dp,
                    vertical = 15.dp
                ),
            long = remember {
                {
                    queueControllerEffect.long(itemEffect);
                }
            },
            click = remember {
                {
                    queueControllerEffect.click(itemEffect);
                }
            }
        );
    }
}