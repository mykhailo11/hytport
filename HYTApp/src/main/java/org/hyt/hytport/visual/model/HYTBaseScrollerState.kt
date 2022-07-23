package org.hyt.hytport.visual.model

import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.hyt.hytport.visual.api.model.HYTScrollerState
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

class HYTBaseScrollerState public constructor(
    initialOffset: Int = 0,
    recycler: RecyclerView?,
    executor: ScheduledExecutorService,
): HYTScrollerState {

    private var _offset: Int by mutableStateOf(initialOffset);

    private val _recycler: RecyclerView? = recycler;

    private val _layout: LinearLayoutManager? by derivedStateOf {
        recycler?.layoutManager
                as LinearLayoutManager?;
    };

    private var _barHeight: Int by mutableStateOf(0);

    private var _scrollerHeight: Int by mutableStateOf(0);

    private var _finish: Int by mutableStateOf(0);

    private var _threshold: Float by mutableStateOf(1.0f);

    private var _scrollerScale: Float by mutableStateOf(1.0f);

    private val _executor: ScheduledExecutorService = executor;

    private var _scheduled: ScheduledFuture<*>? = null;

    private var _total: Int by mutableStateOf(1);

    private var _scrollerState: ScrollableState? = null;

    private var _fastScroll: Boolean by mutableStateOf(false);

    override fun offset(): Int {
        return _offset;
    }

    override fun offset(offset: Int) {
        _offset = offset;
    }

    override fun scroll(delta: Float) {
        val pixels: Int = delta.roundToInt();
        val offset: Int = _offset + pixels;
        val count: Int = _layout?.itemCount ?: 0;
        val scroll: Int = (offset.toFloat() / _finish * count).roundToInt();
        _offset = if (offset < 0) {
            0;
        } else if (offset > _finish) {
            _finish;
        } else {
            offset;
        }
        _fastScroll(delta);
        if (!_fastScroll) {
            _recycler?.scrollToPosition(
                if (scroll >= count) count - 1 else scroll
            );
        } else {
            _scheduled?.cancel(true);
            _scheduled = _executor.schedule(
                {
                    _fastScroll = false;
                    _scheduled = null;
                    _recycler?.scrollToPosition(
                        if (scroll >= count) count - 1 else scroll
                    );
                },
                100,
                TimeUnit.MILLISECONDS
            )
        }
    }

    override fun onScroll() {
        if (_scrollerState?.isScrollInProgress != true && !_fastScroll && _layout != null) {
            val first: Int = _layout!!.findFirstVisibleItemPosition();
            val last: Int = _layout!!.findLastVisibleItemPosition();
            val offset: Int = _evaluate(first, last, _layout!!.itemCount);
            _offset = if (offset < 0 && first != RecyclerView.NO_POSITION) {
                0;
            } else if (offset > _finish || (last == _layout!!.itemCount - 1 && offset > 0)) {
                _finish;
            } else {
                offset;
            }
        }
    }

    override fun barHeight(): Int {
        return _barHeight;
    }

    override fun barHeight(height: Int) {
        _barHeight = height;
        adjust();
        _scale();
    }

    override fun scrollerHeight(): Int {
        return _scrollerHeight;
    }

    override fun scrollerHeight(height: Int) {
        _scrollerHeight = height;
        _finish();
    }

    override fun totalUpdate() {
        _total = _recycler?.adapter?.itemCount ?: 1;
        _scale();
    }

    private fun _scale(): Unit {
        _threshold = if (_layout != null) {
            (1.0f + _layout!!.findLastVisibleItemPosition()
                    - _layout!!.findFirstVisibleItemPosition()).toFloat() / _total.toFloat();
        } else {
            0.0f
        }
        _scrollerScale = if (_threshold < 0.1f) {
            0.1f;
        } else {
            _threshold
        }
    }

    override fun scrollerScale(): Float {
        return _scrollerScale;
    }

    override fun scroller(): ScrollableState? {
        return _scrollerState;
    }

    override fun scroller(scroller: ScrollableState) {
        _scrollerState = scroller;
    }

    override fun fastScroll(): Boolean {
        return _fastScroll;
    }

    override fun fastScroll(fast: Boolean) {
        _fastScroll = fast;
    }

    private fun _finish(): Unit {
        val value = _barHeight - _scrollerHeight
        _finish = when {
            value <= 0 -> 1
            else -> value
        }
    }

    private fun _fastScroll(delta: Float): Unit {
        if (!_fastScroll) {
            _fastScroll = (delta / _scrollerHeight).absoluteValue > _threshold * 5.0f;
        }
    }

    override fun adjust(): Unit {
        if (_layout != null && _scrollerState?.isScrollInProgress != true) {
            val first: Int = _layout!!.findFirstCompletelyVisibleItemPosition();
            val last: Int = _layout!!.findLastVisibleItemPosition();
            _offset = _evaluate(first, last, _layout!!.itemCount);
        } else if (_scrollerState?.isScrollInProgress != true){
            _offset = 0;
        }
    }

    private fun _evaluate(first: Int, last: Int, all: Int): Int {
        val visible: Int = last - first;
        val endBlock: Float = all.toFloat() - visible.toFloat();
        return if (endBlock > 0.0f) {
            (first.toFloat() / endBlock * _finish).roundToInt();
        } else {
            0
        };
    }

}