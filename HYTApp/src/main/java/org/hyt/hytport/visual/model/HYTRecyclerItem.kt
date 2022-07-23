package org.hyt.hytport.visual.model

import android.graphics.Canvas
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import org.hyt.hytport.audio.api.model.HYTAudioModel
import org.hyt.hytport.visual.api.model.HYTItemController

class HYTRecyclerItem(
    controller: HYTItemController
) : ItemTouchHelper.SimpleCallback(
    ItemTouchHelper.UP
            or ItemTouchHelper.DOWN
            or ItemTouchHelper.LEFT
            or ItemTouchHelper.RIGHT,
    0
) {

    private val _controller: HYTItemController;

    private var _move: Boolean = false;

    private var _top: Int = 0;

    private var _start: Int = 0;

    init {
        _controller = controller;
    }

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        return makeMovementFlags(
            ItemTouchHelper.UP
                    or ItemTouchHelper.DOWN
                    or ItemTouchHelper.LEFT
                    or ItemTouchHelper.RIGHT,
            ItemTouchHelper.RIGHT
        )
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val holder: HYTAdapter.Companion.HYTHolder = viewHolder as HYTAdapter.Companion.HYTHolder;
        holder.item?.let { item: HYTAudioModel ->
            _controller.remove(item);
        }
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        val holder: HYTAdapter.Companion.HYTHolder? = viewHolder as HYTAdapter.Companion.HYTHolder?;
        if (holder == null) {
            _controller.move(0.0f, 0.0f, 0.0f, 0.0f);
        }
        _move = _controller.focus(holder?.item, actionState == ItemTouchHelper.ACTION_STATE_DRAG);
        super.onSelectedChanged(viewHolder, actionState)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        val movingIndex: Int = viewHolder.layoutPosition;
        val adapter: HYTAdapter = (recyclerView.adapter as HYTAdapter);
        val movingAudio: HYTAudioModel = adapter.getAudio(movingIndex);
        _move = _controller.focus(movingAudio, true);
        if (_move) {
            val hoveringIndex: Int = target.layoutPosition;
            val hoveringAudio: HYTAudioModel = adapter.getAudio(hoveringIndex);
            adapter.move(movingAudio, hoveringAudio);
        }
        return _move;
    }

    public fun setTop(top: Int): Unit {
        _top = top;
    }

    public fun setStart(start: Int): Unit {
        _start = start;
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val view: View = (viewHolder as HYTAdapter.Companion.HYTHolder).view;
        if (isCurrentlyActive) {
            _controller.move(dX + _start, dY + _top + view.top, view.width.toFloat(), view.height.toFloat());
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }



    override fun interpolateOutOfBoundsScroll(
        recyclerView: RecyclerView,
        viewSize: Int,
        viewSizeOutOfBounds: Int,
        totalSize: Int,
        msSinceStartScroll: Long
    ): Int {
        return if (_controller.selected() == null) {
            super.interpolateOutOfBoundsScroll(
                recyclerView,
                viewSize,
                viewSizeOutOfBounds,
                totalSize,
                msSinceStartScroll
            );
        } else {
            0;
        }
    }

}