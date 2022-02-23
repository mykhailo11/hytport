package org.hyt.hytport.graphics.api.model

import javax.microedition.khronos.opengles.GL10

interface HYTGLProgram {

    fun getProgram(): Int;

    fun setProgram(program: Int): Unit;

    fun getOutColors(): Array<String>;

    fun getGlobalAttributes(): Array<HYTGLStaticAttribute>;

    fun setGlobalAttributes(globalAttributes: Array<HYTGLStaticAttribute>): Unit;

    fun delete(gl: GL10): Unit;

    fun use(gl: GL10, resources: Map<HYTGLData, () -> Unit>): Unit;

}