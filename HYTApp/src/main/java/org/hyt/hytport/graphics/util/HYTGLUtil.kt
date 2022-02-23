package org.hyt.hytport.graphics.util

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class HYTGLUtil {

    companion object{
        fun getDirectFloatBuffer(data: FloatArray): FloatBuffer{
            val buffer: FloatBuffer = ByteBuffer.allocateDirect(data.size * Float.SIZE_BYTES)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
            buffer.put(data);
            buffer.position(0);
            return buffer;
        }
    }

}