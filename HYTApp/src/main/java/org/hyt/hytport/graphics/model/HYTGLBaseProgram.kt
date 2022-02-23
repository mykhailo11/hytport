package org.hyt.hytport.graphics.model

import android.opengl.GLES30
import org.hyt.hytport.graphics.api.model.HYTGLAttribute
import org.hyt.hytport.graphics.api.model.HYTGLData
import org.hyt.hytport.graphics.api.model.HYTGLProgram
import org.hyt.hytport.graphics.api.model.HYTGLStaticAttribute
import org.hyt.hytport.graphics.util.HYTGLUtil
import javax.microedition.khronos.opengles.GL10

class HYTGLBaseProgram public constructor(
    gl: GL10,
    vertexShader: String,
    fragmentShader: String
) : HYTGLProgram {

    private var _program: Int;

    private lateinit var _globalAttributes: Array<HYTGLStaticAttribute>;

    init {
        _program = 0;
        val vertexShaderId: Int = GLES30.glCreateShader(GLES30.GL_VERTEX_SHADER);
        val fragmentShaderId: Int = GLES30.glCreateShader(GLES30.GL_FRAGMENT_SHADER);
        try {
            GLES30.glShaderSource(
                vertexShaderId,
                vertexShader
            );
            GLES30.glShaderSource(
                fragmentShaderId,
                fragmentShader
            );
            GLES30.glCompileShader(vertexShaderId);
            GLES30.glCompileShader(fragmentShaderId);
            _program = GLES30.glCreateProgram();
            GLES30.glAttachShader(_program, vertexShaderId);
            GLES30.glAttachShader(_program, fragmentShaderId);
            GLES30.glLinkProgram(_program);
            GLES30.glDeleteShader(vertexShaderId);
            GLES30.glDeleteShader(fragmentShaderId);
        } catch (exception: Exception) {
            exception.printStackTrace();
        }
    }

    override fun getProgram(): Int {
        return _program;
    }

    override fun setProgram(program: Int) {
        _program = program;
    }

    override fun getOutColors(): Array<String> {
        return arrayOf("fragmentColor");
    }

    override fun getGlobalAttributes(): Array<HYTGLStaticAttribute> {
        return _globalAttributes;
    }

    override fun setGlobalAttributes(globalAttributes: Array<HYTGLStaticAttribute>) {
        _globalAttributes = globalAttributes;
    }

    override fun delete(gl: GL10) {
        GLES30.glDeleteProgram(_program);
    }

    override fun use(gl: GL10, resources: Map<HYTGLData, () -> Unit>) {
        GLES30.glUseProgram(_program);
        _setUniforms(gl, _globalAttributes);
        resources.keys.forEach {
            _setUniforms(gl, it.getStaticAttributes());
            val vao: Int = it.getData();
            if (vao != 0) {
                GLES30.glBindVertexArray(vao);
            }
            try {
                resources[it]?.invoke();
            } catch (exception: Exception) {
                exception.printStackTrace();
            }
        }
    }

    private fun _setUniforms(gl: GL10, attributes: Array<HYTGLStaticAttribute>): Unit {
        if (attributes != null) {
            attributes.forEach {
                val location: Int = GLES30.glGetUniformLocation(_program, it.getName());
                val data: FloatArray = (it.getStaticData() as Array<Float>).toFloatArray();
                val dataSize: Int = it.getChunks();
                when (it.getChunk()) {
                    1 -> GLES30.glUniform1fv(
                        location,
                        dataSize,
                        HYTGLUtil.getDirectFloatBuffer(data)
                    )
                    2 -> GLES30.glUniform2fv(
                        location,
                        dataSize,
                        HYTGLUtil.getDirectFloatBuffer(data)
                    )
                    3 -> GLES30.glUniform3fv(
                        location,
                        dataSize,
                        HYTGLUtil.getDirectFloatBuffer(data)
                    )
                    4 -> GLES30.glUniform4fv(
                        location,
                        dataSize,
                        HYTGLUtil.getDirectFloatBuffer(data)
                    )
                }
            }
        }
    }

}