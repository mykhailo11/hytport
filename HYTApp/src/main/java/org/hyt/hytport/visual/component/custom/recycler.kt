package org.hyt.hytport.visual.component.custom

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemAnimator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.hyt.hytport.R
import org.hyt.hytport.audio.api.model.HYTAudioManager
import org.hyt.hytport.audio.api.model.HYTAudioModel
import org.hyt.hytport.visual.api.model.HYTItemController
import org.hyt.hytport.visual.api.model.HYTScrollerState
import org.hyt.hytport.visual.model.HYTAdapter
import org.hyt.hytport.visual.model.HYTRecycler
import org.hyt.hytport.visual.model.HYTRecyclerItem
import java.util.concurrent.ScheduledExecutorService
import kotlin.math.roundToInt

@Composable
fun recycler(
    executor: ScheduledExecutorService,
    manager: HYTAudioManager?,
    modifier: Modifier = Modifier,
    controller: HYTItemController,
    offset: Offset? = null,
    item: @Composable (HYTAudioModel) -> Unit,
) {
    val density: Density = LocalDensity.current;
    var position: Offset by remember { mutableStateOf(Offset.Zero) };
    val topOffset: Dp by remember(offset) {
        derivedStateOf {
            with(density) {
                (offset?.x?.roundToInt() ?: 0).toDp()
            }
        }
    }
    val bottomOffset: Dp by remember(offset) {
        derivedStateOf {
            with(density) {
                (offset?.y?.roundToInt() ?: 0).toDp()
            }
        }
    }
    var view: HYTRecycler? by remember { mutableStateOf(null) };
    val scrollerState: HYTScrollerState = rememberScrollerState(
        recycler = view,
        executor = executor
    )
    val viewEffect: HYTRecycler? by rememberUpdatedState(view);
    val filterProcess: CoroutineScope = rememberCoroutineScope();
    DisposableEffect(controller, scrollerState) {
        val auditor: HYTItemController.Companion.HYTAuditor = object :
            HYTItemController.Companion.HYTAuditor {

            override fun onMove(start: Float, top: Float, width: Float, height: Float) {
                viewEffect?.setOffset(start, top);
                viewEffect?.setSize(width, height);
            }

            override fun onRemove(audio: HYTAudioModel) {
                (viewEffect?.adapter as HYTAdapter?)?.remove(audio);
            }

            override fun onFocus(audio: HYTAudioModel?, move: Boolean): Boolean {
                (viewEffect?.parent as ViewGroup?)?.clipChildren = audio == null || !move;
                return true;
            }

            override fun onFilter(filter: ((HYTAudioModel) -> Boolean)?) {
                filterProcess.launch {
                    (viewEffect?.adapter as HYTAdapter?)?.filter(filter);
                    scrollerState.totalUpdate();
                }
            }

        };
        controller.addAuditor(auditor);
        onDispose {
            controller.removeAuditor(auditor);
        }
    }
    var callback: HYTRecyclerItem? by remember { mutableStateOf(null) };
    LaunchedEffect(manager) {
        scrollerState.totalUpdate();
    };
    LaunchedEffect(callback, position) {
        callback?.setTop(position.y.roundToInt());
        callback?.setStart(position.x.roundToInt());
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
    ) {
        AndroidView(
            factory = { context: Context ->
                val layout: LinearLayoutManager = LinearLayoutManager(context);
                layout.isMeasurementCacheEnabled = false;
                val inflater: LayoutInflater = context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater;
                val recycler: HYTRecycler = inflater
                    .inflate(R.layout.hyt_recycler, null) as HYTRecycler;
                val adapter: HYTAdapter = HYTAdapter(
                    context = context,
                    item = item,
                    manager = manager,
                    recycler = recycler
                );
                recycler.setRound(0.3f);
                callback = HYTRecyclerItem(controller);
                val itemHandler: ItemTouchHelper = ItemTouchHelper(callback!!);
                recycler.adapter = adapter;
                recycler.layoutManager = layout;
                recycler.layoutParams = layout.generateDefaultLayoutParams();
                itemHandler.attachToRecyclerView(recycler);
                recycler.clipToPadding = false;
                recycler.clipChildren = false;
                val animator: ItemAnimator? = recycler.itemAnimator;
                animator?.removeDuration = animator?.removeDuration?.plus(animator.moveDuration) ?: 0;
                view = recycler;
                recycler;
            },
            update = { recycler: RecyclerView ->
                recycler.setPadding(
                    0,
                    0,
                    0,
                    offset?.y?.roundToInt() ?: 0
                );
                val adapter: HYTAdapter = recycler.adapter as HYTAdapter;
                adapter.setManager(manager);
            },
            modifier = Modifier
                .weight(1.0f)
                .zIndex(10.0f)
                .padding(
                    top = topOffset
                )
                .onGloballyPositioned { coordinates: LayoutCoordinates ->
                    position = coordinates.positionInWindow();
                }
        );
        scroller(
            scrollerState = scrollerState,
            modifier = Modifier
                .padding(
                    start = 0.dp,
                    end = 0.dp,
                    top = 10.dp + topOffset,
                    bottom = 10.dp + bottomOffset
                )
        );
    }
}