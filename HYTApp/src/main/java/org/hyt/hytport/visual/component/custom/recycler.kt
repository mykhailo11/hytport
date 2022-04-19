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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.hyt.hytport.R
import org.hyt.hytport.audio.api.model.HYTAudioModel
import java.util.concurrent.ScheduledExecutorService

class HYTAdapter(
    context: Context,
    items: List<HYTAudioModel>,
    item: @Composable (HYTAudioModel) -> Unit
) : RecyclerView.Adapter<HYTAdapter.Companion.HYTHolder>() {

    companion object {

        class HYTHolder(
            view: ComposeView
        ) : RecyclerView.ViewHolder(view) {

            val view: ComposeView;

            init {
                this.view = view;
            }

        }

    }

    private var _items: List<HYTAudioModel>;

    private val _item: @Composable (HYTAudioModel) -> Unit

    private var _filtered: MutableList<HYTAudioModel>;

    private val _context: Context;

    init {
        _items = items;
        _filtered = items.toMutableList();
        _item = item;
        _context = context;
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
    }

    override fun getItemCount(): Int {
        return _filtered.count();
    }

    public fun filter(filter: (HYTAudioModel) -> Boolean): Unit {
        _items.forEach { audio: HYTAudioModel ->
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
                    }else {
                        _filtered.add(0, audio);
                        notifyItemInserted(0);
                    }
                }
            }
        };
    }

    public fun getItems(): List<HYTAudioModel> {
        return _items;
    }

    public fun setItems(items: List<HYTAudioModel>): Unit {
        _items = items;
    }

}

@Composable
fun recycler(
    executor: ScheduledExecutorService,
    items: List<HYTAudioModel>,
    filter: (HYTAudioModel) -> Boolean,
    modifier: Modifier = Modifier,
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
                        items = items
                    );
                    val inflater: LayoutInflater = context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater;
                    val recycler: RecyclerView = inflater
                        .inflate(R.layout.hyt_recycler, null) as RecyclerView;
                    recycler.adapter = adapter;
                    recycler.layoutManager = LinearLayoutManager(context);
                    view = recycler;
                    recycler;
                },
                update = { recycler: RecyclerView ->
                    val adapter: HYTAdapter = recycler.adapter as HYTAdapter;
                    adapter.setItems(items);
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