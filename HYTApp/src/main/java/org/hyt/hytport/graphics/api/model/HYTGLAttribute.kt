package org.hyt.hytport.graphics.api.model

interface HYTGLAttribute {

    fun getName(): String;

    fun setName(name: String): Unit;

    fun getChunk(): Int;

    fun setChunk(chunk: Int): Unit;

    fun getChunks(): Int;

    fun setChunks(chunks: Int): Unit;

}