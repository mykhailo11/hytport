package org.hyt.hytport.visual.model

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import org.hyt.hytport.R
import org.hyt.hytport.audio.api.model.HYTAudioModel
import org.hyt.hytport.util.HYTUtil
import java.util.*

class HYTAudioModelAdapter(
    context: Context,
    queue: Deque<HYTAudioModel>,
    click: (HYTAudioModel) -> Unit
): BaseAdapter() {

    private class Item public constructor(
        title: TextView,
        artist: TextView,
        album: ImageView
    ){

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

    init {
        _queue = queue;
        _context = context;
        _click = click;
    }

    override fun getCount(): Int {
        return _queue.count();
    }

    override fun getItem(index: Int): Any {
        return _queue.elementAt(index);
    }

    override fun getItemId(index: Int): Long {
        val id: Long? = _queue.elementAt(index).getId();
        if (id != null){
            return id;
        }else {
            return -1L;
        }
    }

    override fun getView(index: Int, view: View?, parent: ViewGroup?): View {
        if (view == null){
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

    private fun _getView(index: Int, view: View, parent: ViewGroup?): View{
        val item: Item = view.tag as Item;
        val audio: HYTAudioModel = _queue.elementAt(index);
        view.setOnClickListener {
            _click.invoke(audio);
        }
        item.artist.text = audio.getArtist();
        val album: Bitmap? = HYTUtil.getBitmap(audio.getAlbumPath(), _context.contentResolver);
        if (album != null){
            item.album.setImageBitmap(album);
        }else{
            item.album.setImageResource(R.drawable.hyt_empty_cover_200dp);
        }
        item.title.text = audio.getTitle();
        return view;
    }

}