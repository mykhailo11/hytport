package org.hyt.hytport.graphics.model

import org.hyt.hytport.graphics.api.model.HYTGLStaticAttribute

class HYTGLBaseAttribute public constructor(
    name: String,
    chunk: Int,
    chunks: Int
): HYTGLStaticAttribute {

    private var _name: String;

    private var _chunk: Int;

    private var _chunks: Int;

    private lateinit var _data: Any;

    init {
        _name = name;
        _chunk = chunk;
        _chunks = chunks;
    }

    override fun getName(): String {
        return _name;
    }

    override fun setName(name: String) {
        _name = name;
    }

    override fun getChunk(): Int {
        return _chunk;
    }

    override fun setChunk(chunk: Int) {
        _chunk = chunk;
    }

    override fun getChunks(): Int {
        return _chunks;
    }

    override fun setChunks(chunks: Int) {
        _chunks = chunks;
    }

    override fun getStaticData(): Any {
        return _data;
    }

    override fun setStaticData(data: Any) {
        _data = data;
    }
}