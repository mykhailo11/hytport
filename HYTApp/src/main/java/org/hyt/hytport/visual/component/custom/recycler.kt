package org.hyt.hytport.visual.component.custom

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.hyt.hytport.R
import org.hyt.hytport.audio.api.model.HYTAudioManager
import org.hyt.hytport.audio.api.model.HYTAudioModel
import java.util.*
import java.util.concurrent.ScheduledExecutorService
import kotlin.collections.ArrayList

class HYTItemTouch(
    focus: (HYTAudioModel?, HYTAudioModel?) -> Boolean
) : ItemTouchHelper.SimpleCallback(
    ItemTouchHelper.UP
            or ItemTouchHelper.DOWN
            or ItemTouchHelper.LEFT
            or ItemTouchHelper.RIGHT,
    0
) {

    private val _focus: (HYTAudioModel?, HYTAudioModel?) -> Boolean;

    init {
        _focus = focus;
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        if (
            viewHolder == null
            || actionState == ItemTouchHelper.ACTION_STATE_IDLE
            || actionState == ItemTouchHelper.ACTION_STATE_SWIPE
        ) {
            _focus(null, null);
        } else if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            _focus((viewHolder as HYTAdapter.Companion.HYTHolder).item, null);
        }
        super.onSelectedChanged(viewHolder, actionState)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        val movingIndex: Int = viewHolder.adapterPosition;
        val hoveringIndex: Int = target.adapterPosition;
        val adapter: HYTAdapter = (recyclerView.adapter as HYTAdapter);
        val movingAudio: HYTAudioModel = adapter.getAudio(movingIndex);
        val hoveringAudio: HYTAudioModel = adapter.getAudio(hoveringIndex);
        val move: Boolean = _focus(movingAudio, hoveringAudio);
        if (move) {
            (recyclerView.adapter as HYTAdapter).move(movingAudio, hoveringAudio);
        }
        return move;
    }


}

class HYTAdapter(
    context: Context,
    manager: HYTAudioManager,
    item: @Composable (HYTAudioModel) -> Unit
) : RecyclerView.Adapter<HYTAdapter.Companion.HYTHolder>() {

    companion object {

        class HYTHolder(
            view: ComposeView
        ) : RecyclerView.ViewHolder(view) {

            val view: ComposeView;

            var item: HYTAudioModel? = null;

            init {
                this.view = view;
            }

        }

    }

    private var _manager: HYTAudioManager;

    private val _item: @Composable (HYTAudioModel) -> Unit

    private var _filtered: MutableList<HYTAudioModel> = ArrayList();

    private val _context: Context;

    init {
        _manager = manager;
        _manager.queue { items: MutableList<HYTAudioModel> ->
            _filtered.addAll(items);
            notifyDataSetChanged();
        }
        _item = item;
        _context = context;
    }

    override fun getItemId(position: Int): Long {
        return _filtered[position].getId();
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HYTHolder {
        val view: ComposeView = ComposeView(_context);
        return HYTHolder(view);
    }

    override fun onBindViewHolder(holder: HYTHolder, position: Int) {
        val audio: HYTAudioModel = _filtered[position];
        holder.view.setContent {
            _item(audio);
        }
        holder.item = audio;
    }

    override fun getItemCount(): Int {
        return _filtered.count();
    }

    public fun filter(filter: (HYTAudioModel) -> Boolean): Unit {
        _manager.queue { items: List<HYTAudioModel> ->
            items.forEach { audio: HYTAudioModel ->
                val appeal: Boolean = filter(audio);
                val index: Int = _filtered.indexOf(audio);
                when {
                    !appeal && index != -1 -> {
                        _filtered.removeAt(index);
                        notifyItemRemoved(index);
                    }
                    appeal && index == -1 -> {
                        val next: Int = _filtered.indexOfFirst { filtered: HYTAudioModel ->
                            filtered.getOrder() > audio.getOrder();
                        }
                        if (next > 0) {
                            _filtered.add(next - 1, audio);
                            notifyItemInserted(next - 1);
                        } else {
                            _filtered.add(0, audio);
                            notifyItemInserted(0);
                        }
                    }
                }
            };
        }
    }

    public fun getAudio(position: Int): HYTAudioModel {
        return _filtered[position];
    }

    public fun move(from: HYTAudioModel, to: HYTAudioModel): Unit {
        val fromFilteredIndex: Int = _filtered.indexOf(from);
        val toFilteredIndex: Int = _filtered.indexOf(to);
        if (fromFilteredIndex != -1 && toFilteredIndex != -1) {
            Collections.swap(_filtered, fromFilteredIndex, toFilteredIndex);
        }
        _manager.queue { items: MutableList<HYTAudioModel> ->
            val fromIndex: Int = items.indexOf(from);
            val toIndex: Int = items.indexOf(to);
            if (fromIndex != -1 && toIndex != -1) {
                Collections.swap(items, fromIndex, toIndex);
            }
        }
        notifyItemMoved(fromFilteredIndex, toFilteredIndex);
    }

    public fun getFiltered(): List<HYTAudioModel> {
        return _filtered;
    }

    public fun setManager(manager: HYTAudioManager): Unit {
        _manager = manager;
    }

}

@Composable
fun recycler(
    executor: ScheduledExecutorService,
    manager: HYTAudioManager,
    filter: (HYTAudioModel) -> Boolean,
    modifier: Modifier = Modifier,
    focus: (HYTAudioModel?, HYTAudioModel?) -> Boolean,
    item: @Composable (HYTAudioModel) -> Unit,
) {
    var view: RecyclerView? by remember { mutableStateOf(null) };
    BoxWithConstraints(
        modifier = modifier
    ) {
        Row {
            AndroidView(
                factory = { context: Context ->
                    val adapter: HYTAdapter = HYTAdapter(
                        context = context,
                        item = item,
                        manager = manager
                    );
                    val inflater: LayoutInflater = context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater;
                    val recycler: RecyclerView = inflater
                        .inflate(R.layout.hyt_recycler, null) as RecyclerView;
                    recycler.adapter = adapter;
                    recycler.layoutManager = LinearLayoutManager(context);
                    val itemHandler: ItemTouchHelper = ItemTouchHelper(HYTItemTouch(focus));
                    itemHandler.attachToRecyclerView(recycler);
                    view = recycler;
                    recycler;
                },
                update = { recycler: RecyclerView ->
                    val adapter: HYTAdapter = recycler.adapter as HYTAdapter;
                    adapter.setManager(manager);
                    adapter.filter(filter);
                },
                modifier = Modifier
                    .weight(1.0f)
            );
            if (view != null) {
                scroller(
                    executor = executor,
                    recycler = view!!
                );
            }
        }
    }
}