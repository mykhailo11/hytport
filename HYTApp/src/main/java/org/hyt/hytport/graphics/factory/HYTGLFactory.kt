package org.hyt.hytport.graphics.factory

import org.hyt.hytport.graphics.api.model.*
import org.hyt.hytport.graphics.model.HYTGLBaseAttribute
import org.hyt.hytport.graphics.model.HYTGLBaseBuffer
import org.hyt.hytport.graphics.model.HYTGLBaseData
import org.hyt.hytport.graphics.model.HYTGLBaseProgram
import javax.microedition.khronos.opengles.GL10

class HYTGLFactory {

    companion object{

        fun getAttribute(name: String, chunk: Int, chunks: Int): HYTGLStaticAttribute{
            return HYTGLBaseAttribute(name, chunk, chunks);
        }

        fun getBuffer(gl: GL10): HYTGLBuffer{
            return HYTGLBaseBuffer(gl);
        }

        fun getData(gl: GL10): HYTGLData{
            return HYTGLBaseData(gl);
        }

        fun getProgram(
            gl: GL10,
            vertexShader: String,
            fragmentShader: String
        ): HYTGLProgram{
            return HYTGLBaseProgram(gl, vertexShader, fragmentShader);
        }

    }

}