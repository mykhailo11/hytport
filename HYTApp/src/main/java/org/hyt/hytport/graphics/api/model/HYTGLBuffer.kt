package org.hyt.hytport.graphics.api.model

import javax.microedition.khronos.opengles.GL10

interface HYTGLBuffer {

    fun getBuffer(): Int;

    fun setBuffer(buffer: Int): Unit;

    fun setData(gl: GL10, data: Array<Float>): Unit;

    fun delete(gl: GL10): Unit;

}