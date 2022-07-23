package org.hyt.hytport.visual.model

import android.content.Context
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.recyclerview.widget.RecyclerView
import org.hyt.hytport.audio.api.model.HYTAudioManager
import org.hyt.hytport.audio.api.model.HYTAudioModel
import java.util.*
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class HYTAdapter(
    context: Context,
    manager: HYTAudioManager?,
    recycler: HYTRecycler,
    item: @Composable (HYTAudioModel) -> Unit
) : RecyclerView.Adapter<HYTAdapter.Companion.HYTHolder>() {

    companion object {

        class HYTHolder(
            view: ComposeView,
        ) : RecyclerView.ViewHolder(view) {

            val view: ComposeView;

            var item: HYTAudioModel? = null;

            var ready: Boolean = false;

            init {
                this.view = view;
            }

            fun drop(): Unit {
                view.disposeComposition();
            }

        }

    }

    private var _manager: HYTAudioManager?;

    private val _item: @Composable (HYTAudioModel) -> Unit

    private var _filtered: MutableList<HYTAudioModel> = ArrayList();

    private var _filter: Boolean = true;

    private val _context: Context;

    private val _recycler: HYTRecycler;

    init {
        _manager = manager;
        _manager?.queue { items: MutableList<HYTAudioModel> ->
            _filtered.addAll(items);
            notifyDataSetChanged();
        }
        _item = item;
        _context = context;
        _recycler = recycler;
    }

    override fun getItemId(position: Int): Long {
        return _filtered[position].getId();
    }

    override fun onViewDetachedFromWindow(holder: HYTHolder) {
        holder.drop();
        super.onViewDetachedFromWindow(holder)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HYTHolder {
        val holder: HYTHolder = HYTHolder(ComposeView(_context));
        return holder;
    }

    override fun onBindViewHolder(holder: HYTHolder, position: Int) {
        val audio: HYTAudioModel = _filtered[position];
        holder.view.setContent {
            _item(audio);
        }
        holder.ready = true;
        holder.item = audio;
    }

    override fun getItemCount(): Int {
        return _filtered.count();
    }

    public fun filter(filter: ((HYTAudioModel) -> Boolean)?): Unit {
        if (filter == null && _filter) {
            _filter = false;
            _manager?.queue { items: List<HYTAudioModel> ->
                _filtered.clear();
                _filtered.addAll(items);
                notifyDataSetChanged();
            }
        } else if (filter != null) {
            _filter = true;
            _filter(filter);
        }
    }

    private fun _filter(filter: (HYTAudioModel) -> Boolean): Unit {
        _manager?.queue { items: List<HYTAudioModel> ->
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
        _manager?.queue { items: MutableList<HYTAudioModel> ->
            val fromIndex: Int = items.indexOf(from);
            val toIndex: Int = items.indexOf(to);
            if (fromIndex != -1 && toIndex != -1) {
                Collections.swap(items, fromIndex, toIndex);
            }
            notifyItemMoved(fromFilteredIndex, toFilteredIndex);
        }
    }

    fun remove(audio: HYTAudioModel): Unit {
        _manager?.queue { queue: MutableList<HYTAudioModel> ->
            val index: Int = _filtered.indexOfFirst { item: HYTAudioModel ->
                item.getId() == audio.getId();
            }
            queue.removeIf { item: HYTAudioModel ->
                item.getId() == audio.getId();
            }
            if (index != -1) {
                _filtered.removeAt(index);
                notifyItemRemoved(index);
            }
        }
    }

    fun changed(audio: HYTAudioModel): Unit {
        val index: Int = _filtered.indexOfFirst { item: HYTAudioModel ->
            item.getId() == audio.getId();
        }
        notifyItemChanged(index);
    }

    public fun getFiltered(): List<HYTAudioModel> {
        return _filtered;
    }

    public fun setManager(manager: HYTAudioManager?): Unit {
        if (_manager?.name() != manager?.name()) {
            _manager = manager;
            _manager?.queue { items: MutableList<HYTAudioModel> ->
                _filtered.clear();
                _filtered.addAll(items);
                notifyDataSetChanged();
            }
        }
    }

}