package org.hyt.hytport.graphics.model

import android.opengl.GLES30
import org.hyt.hytport.graphics.api.model.*
import java.nio.IntBuffer
import javax.microedition.khronos.opengles.GL10

class HYTGLBaseData public constructor(gl: GL10): HYTGLData{

    private var _data: Int;

    private lateinit var _buffer: HYTGLBuffer;

    private var _attributes: Array<HYTGLAttribute>;

    private var _staticAttributes: Array<HYTGLStaticAttribute>;

    init {
        val id: IntBuffer = IntBuffer.allocate(1);
        GLES30.glGenVertexArrays(1, id);
        _data = id.get();
        _attributes = arrayOf();
        _staticAttributes = arrayOf();
    }

    override fun getData(): Int {
        return _data;
    }

    override fun setData(data: Int) {
        _data = data;
    }

    override fun getBuffer(): HYTGLBuffer {
        return _buffer;
    }

    override fun setBuffer(gl: GL10, program: HYTGLProgram, buffer: HYTGLBuffer): HYTGLBuffer {
        val initial: HYTGLBuffer = _buffer;
        _buffer = buffer;
        GLES30.glBindVertexArray(_data);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, _buffer.getBuffer());
        if (_attributes != null){
            var offset: Int = 0;
            _attributes.forEach {
                val location: Int = GLES30.glGetAttribLocation(
                    program.getProgram(),
                    it.getName()
                );
                val chunk: Int = it.getChunk();
                GLES30.glEnableVertexAttribArray(location);
                GLES30.glVertexAttribPointer(
                    location,
                    chunk,
                    GLES30.GL_FLOAT,
                    false,
                    0,
                    offset
                );
                offset += chunk * it.getChunks() * Float.SIZE_BYTES;
            }
        }
        return initial;
    }

    override fun getAttributes(): Array<HYTGLAttribute> {
        return _attributes;
    }

    override fun setAttributes(attributes: Array<HYTGLAttribute>) {
        _attributes = attributes;
    }

    override fun getStaticAttributes(): Array<HYTGLStaticAttribute> {
        return _staticAttributes;
    }

    override fun setStaticAttributes(staticAttributes: Array<HYTGLStaticAttribute>) {
        _staticAttributes = staticAttributes;
    }

    override fun delete(gl: GL10): HYTGLBuffer {
        val id: IntBuffer = IntBuffer.allocate(1);
        id.put(0, _data);
        GLES30.glDeleteVertexArrays(1, id);
        return _buffer;
    }
}