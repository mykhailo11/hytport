package org.hyt.hytport.visual.component.custom

import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.runtime.*
import androidx.recyclerview.widget.RecyclerView
import org.hyt.hytport.visual.api.model.HYTScrollerState
import org.hyt.hytport.visual.factory.HYTComponentFactory
import java.util.concurrent.ScheduledExecutorService

@Composable
fun rememberScrollerState(
    recycler: RecyclerView? = null,
    executor: ScheduledExecutorService,
    auditor: ((offset: Int) -> Unit)? = null
): HYTScrollerState {
    val state: HYTScrollerState by remember(recycler, executor) {
        derivedStateOf {
            HYTComponentFactory.getScrollerState(
                recycler = recycler,
                executor = executor
            );
        }
    };
    val scroller: ScrollableState = rememberScrollableState { delta: Float ->
        state.scroll(delta);
        delta;
    };
    val stateEffect: HYTScrollerState by rememberUpdatedState(state);
    DisposableEffect(
        recycler
    ) {
        val scroll: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {

            private var _offset: Int = 0;

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                _offset += dy;
                auditor?.invoke(_offset);
                stateEffect.onScroll();
                super.onScrolled(recyclerView, dx, dy);
            }

        };
        recycler?.addOnScrollListener(scroll);
        onDispose {
            recycler?.removeOnScrollListener(scroll);
        }
    }
    LaunchedEffect(state) {
        state.totalUpdate();
        state.adjust();
        state.scroller(scroller);
    }
    return state;
}