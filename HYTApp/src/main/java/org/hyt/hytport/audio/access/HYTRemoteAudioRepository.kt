package org.hyt.hytport.audio.access

import android.content.Context
import android.net.Uri
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.hyt.hytport.R
import org.hyt.hytport.audio.api.access.HYTAudioRepository
import org.hyt.hytport.audio.api.model.HYTAudioModel
import org.hyt.hytport.audio.factory.HYTAudioFactory
import org.json.JSONObject

class HYTRemoteAudioRepository public constructor(
    base: String,
    endpoints: Map<HYTEndpoints, String>,
    context: Context
): HYTAudioRepository {

    private val _base: String;

    private val _endpoints: Map<HYTEndpoints, String>;

    private val _context: Context;

    private val _requestQueue: RequestQueue;

    companion object {
        public enum class HYTEndpoints{
            GET_ALL,
            GET_BY_ID
        }
    }

    init {
        _base = base;
        _endpoints = endpoints;
        _context = context;
        _requestQueue = Volley.newRequestQueue(_context);
    }

    override fun getAllAudio(ready: (List<HYTAudioModel>) -> Unit): Unit {
        _getAudio("${_base}/${_endpoints[HYTEndpoints.GET_ALL]}", ready);
    }

    override fun getAudioById(id: Any, ready: (HYTAudioModel?) -> Unit): Unit {
        _getAudio("${_base}/${_endpoints[HYTEndpoints.GET_BY_ID]}/${id}"){
            ready(it.first());
        };
    }

    private fun _getAudio(
        path: String,
        ready: (List<HYTAudioModel>) -> Unit
    ): Unit{
        val request: JsonArrayRequest = JsonArrayRequest(
            Request.Method.GET,
            path,
            null,
            { json ->
                val audios: MutableList<HYTAudioModel> = ArrayList();
                for (index in 0 until json.length()){
                    val jsonAudio: JSONObject = json.getJSONObject(index);
                    audios.add(
                        HYTAudioFactory.getAudioModel().apply {
                            val id: Long = jsonAudio.optLong("id");
                            setId(id);
                            setTitle(jsonAudio.optString("title"));
                            setArtist(jsonAudio.optString("artist"));
                            setAlbum(jsonAudio.optString("album"));
                            setDuration(jsonAudio.optLong("duration"));
                            setAlbumPath(Uri.parse(jsonAudio.optString("albumPath")));
                            setPath(Uri.parse("/${id}"));
                        }
                    );
                }
                ready(audios);
            },
            {
                Log.e("http", it.toString());
            }
        );
        request.setShouldCache(false);
        _requestQueue.add(request);
    }

}