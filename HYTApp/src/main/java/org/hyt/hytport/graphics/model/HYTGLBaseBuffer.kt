package org.hyt.hytport.graphics.model

import android.opengl.GLES30
import org.hyt.hytport.graphics.api.model.HYTGLBuffer
import org.hyt.hytport.graphics.util.HYTGLUtil
import java.nio.IntBuffer
import javax.microedition.khronos.opengles.GL10

class HYTGLBaseBuffer public constructor(gl: GL10) : HYTGLBuffer {

    private var _buffer: Int;

    init {
        val id: IntBuffer = IntBuffer.allocate(1);
        GLES30.glGenBuffers(1, id);
        _buffer = id.get();
    }

    override fun getBuffer(): Int {
        return _buffer;
    }

    override fun setBuffer(buffer: Int) {
        _buffer = buffer;
    }

    override fun setData(gl: GL10, data: Array<Float>) {
        val dataSize: Int = data.size * Float.SIZE_BYTES;
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, _buffer);
        GLES30.glBufferData(
            GLES30.GL_ARRAY_BUFFER,
            dataSize,
            HYTGLUtil.getDirectFloatBuffer(data.toFloatArray()),
            GLES30.GL_DYNAMIC_DRAW
        );
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
    }

    override fun delete(gl: GL10) {
        val id: IntBuffer = IntBuffer.allocate(1);
        id.put(0, _buffer);
        GLES30.glDeleteBuffers(1, id);
    }
}