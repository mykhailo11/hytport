package org.hyt.hytport.visual.model

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import org.hyt.hytport.R
import org.hyt.hytport.audio.api.model.HYTAudioModel
import org.hyt.hytport.util.HYTUtil
import java.util.*
import kotlin.reflect.typeOf

class HYTAudioModelAdapter(
    context: Context,
    queue: Deque<HYTAudioModel>,
    click: (HYTAudioModel) -> Unit
) : BaseAdapter(), Filterable {

    private class Item public constructor(
        title: TextView,
        artist: TextView,
        album: ImageView
    ) {

        public val title: TextView;

        public val artist: TextView;

        public val album: ImageView;

        init {
            this.title = title;
            this.artist = artist;
            this.album = album;
        }

    }

    private val _queue: Deque<HYTAudioModel>;

    private val _context: Context;

    private val _click: (HYTAudioModel) -> Unit;

    private val _filtered: MutableList<HYTAudioModel>;

    init {
        _queue = queue;
        _filtered = queue.toMutableList();
        _context = context;
        _click = click;
    }

    override fun getCount(): Int {
        return _filtered.count();
    }

    override fun getItem(index: Int): HYTAudioModel {
        return _filtered.elementAt(index);
    }

    private fun _matches(audio: HYTAudioModel, filter: Regex): Boolean {
        val title: String? = audio.getTitle();
        val artist: String? = audio.getArtist();
        return (title != null && title.matches(filter))
                || (artist != null && artist.matches(filter));
    }

    override fun getItemId(index: Int): Long {
        return _filtered.elementAt(index).getId() ?: -1L;
    }

    override fun getView(index: Int, view: View?, parent: ViewGroup): View {
        if (view == null) {
            val itemView: View = LayoutInflater.from(_context).inflate(R.layout.hyt_library_item, parent, false)
            val itemElements: Item = Item(
                itemView.findViewById(R.id.hyt_item_title),
                itemView.findViewById(R.id.hyt_item_artist),
                itemView.findViewById(R.id.hyt_item_album)
            )
            itemView.tag = itemElements;
            return _getView(index, itemView, parent);
        }
        return _getView(index, view, parent);
    }

    private fun _getView(index: Int, view: View, parent: ViewGroup?): View {
        val item: Item = view.tag as Item;
        val audio: HYTAudioModel = _filtered.elementAt(index);
        view.setOnClickListener {
            _click.invoke(audio);
        }
        item.artist.text = audio.getArtist();
        val album: Bitmap? = HYTUtil.getBitmap(audio.getAlbumPath(), _context.contentResolver);
        if (album != null) {
            item.album.setImageBitmap(album);
        } else {
            item.album.setImageResource(R.drawable.hyt_empty_cover_200dp);
        }
        item.title.text = audio.getTitle();
        return view;
    }

    override fun getFilter(): Filter {

        return object : Filter() {

            override fun performFiltering(chars: CharSequence?): FilterResults {
                val result: FilterResults = FilterResults();
                val content: String = chars.toString();
                val filter: Regex = HYTUtil.anyMatch(content).toRegex();
                val filtered: List<HYTAudioModel> = _queue.filter {
                    _matches(it, filter);
                }
                result.values = filtered;
                result.count = filtered.count();
                return result;
            }

            override fun publishResults(chars: CharSequence?, results: FilterResults?) {
                if (results?.values != null) {
                    _filtered.clear();
                    _filtered.addAll(results.values as List<HYTAudioModel>);
                    notifyDataSetChanged();
                }
            }

        }
    }
}