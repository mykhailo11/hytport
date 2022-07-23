package org.hyt.hytport.visual.component.custom

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.hyt.hytport.visual.api.model.HYTHoverState
import org.hyt.hytport.visual.api.model.HYTItemController

@Composable
fun hoverable(
    state: HYTHoverState,
    controller: HYTItemController,
    modifier: Modifier = Modifier,
    body: @Composable () -> Unit,
): Unit {
    var box: Rect? by remember { mutableStateOf(null) };
    val boxEffect: Rect? by rememberUpdatedState(box);
    val process: CoroutineScope = rememberCoroutineScope();
    val stateEffect: HYTHoverState by rememberUpdatedState(state);
    DisposableEffect(controller) {
        val auditor: HYTItemController.Companion.HYTAuditor = object:
            HYTItemController.Companion.HYTAuditor {

            private var _hovering: Boolean = false;

            private var _job: Job? = null;

            override fun onMove(start: Float, top: Float, width: Float, height: Float) {
                if (_job?.isCompleted == true || _job == null) {
                    _job = process.launch {
                        val actuallyHovering = controller.selected() != null && boxEffect != null
                                && start + width * 0.5f in boxEffect!!.left..boxEffect!!.right
                                && top + height * 0.5f in boxEffect!!.top..boxEffect!!.bottom
                        if (_hovering != actuallyHovering && actuallyHovering) {
                            stateEffect.hover();
                            _hovering = true;
                        } else if (_hovering != actuallyHovering){
                            stateEffect.out();
                            _hovering = false;
                        }
                    }
                }
            }

        }
        controller.addAuditor(auditor);
        onDispose {
            controller.removeAuditor(auditor);
        }
    }
    Box(
        modifier = modifier
            .onGloballyPositioned { coordinates: LayoutCoordinates ->
                val size: IntSize = coordinates.size;
                val position: Offset = coordinates.positionInWindow();
                box = Rect(
                    position,
                    Size(
                        size.width.toFloat(),
                        size.height.toFloat()
                    )
                );
            }
    ){
        body();
    }
}