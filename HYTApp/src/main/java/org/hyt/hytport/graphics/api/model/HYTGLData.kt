package org.hyt.hytport.graphics.api.model

import javax.microedition.khronos.opengles.GL10

interface HYTGLData {

    fun getData(): Int;

    fun setData(data: Int): Unit;

    fun getBuffer(): HYTGLBuffer;

    fun setBuffer(gl: GL10, program: HYTGLProgram, buffer: HYTGLBuffer): HYTGLBuffer;

    fun getAttributes(): Array<HYTGLAttribute>;

    fun setAttributes(attributes: Array<HYTGLAttribute>): Unit;

    fun getStaticAttributes(): Array<HYTGLStaticAttribute>;

    fun setStaticAttributes(staticAttributes: Array<HYTGLStaticAttribute>): Unit;

    fun delete(gl: GL10): HYTGLBuffer;

}