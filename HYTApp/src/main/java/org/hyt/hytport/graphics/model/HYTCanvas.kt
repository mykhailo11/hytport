package org.hyt.hytport.graphics.model

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import org.hyt.hytport.graphics.api.model.HYTGLData
import org.hyt.hytport.graphics.api.model.HYTGLProgram
import org.hyt.hytport.graphics.api.model.HYTGLStaticAttribute
import org.hyt.hytport.graphics.factory.HYTGLFactory
import org.hyt.hytport.visual.api.model.HYTState
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class HYTCanvas public constructor(
    context: Context,
    vertexShader: String,
    fragmentShader: String,
    states: Map<String, Array<HYTState>>
) : GLSurfaceView.Renderer {

    companion object {

        private val _NESTED_PARAMETERS: Int = 2;

        public val OUT_COLOR: String = "fragmentColor";

    }

    private val _context: Context;

    private var _executionTime: Float = 0.0f;

    private var _width: Float;

    private var _height: Float;

    private lateinit var _program: HYTGLProgram;

    private lateinit var _canvas: HYTGLData;

    private val _states: Map<String, Array<HYTState>>;

    private val _vertexShader: String;

    private val _fragmentShader: String;

    init {
        _context = context;
        _executionTime = 0.0f;
        _vertexShader = vertexShader;
        _fragmentShader = fragmentShader;
        _states = states;
        _width = 0.0f;
        _height = 0.0f;
    }

    override fun onSurfaceCreated(gl: GL10, p1: EGLConfig?) {
        _canvas = HYTGLFactory.getData(gl);
        _program = HYTGLFactory.getProgram(gl, _vertexShader, _fragmentShader);
        val data: Array<HYTGLStaticAttribute> = Array(
            _NESTED_PARAMETERS + _states.size
        ) { _: Int ->
            HYTGLFactory.getAttribute("", 0, 0);
        };
        data[0].setName("surface");
        data[0].setChunk(2);
        data[0].setChunks(1);
        data[0].setStaticData(arrayOf(_height, _width));
        data[1].setName("time");
        data[1].setChunk(1);
        data[1].setChunks(1);
        data[1].setStaticData(arrayOf(_executionTime));
        _setStates(data);
        _program.setGlobalAttributes(data);
    }

    private fun _setStates(data: Array<HYTGLStaticAttribute>): Unit {
        val parameters: Set<String> = _states.keys;
        for (state: Int in parameters.indices) {
            val current: Array<HYTState>? = _states[parameters.elementAt(state)];
            val states: Array<Float> = Array(current!!.size) { index: Int ->
                current[index].getState();
            };
            data[state + _NESTED_PARAMETERS].setName(parameters.elementAt(state));
            data[state + _NESTED_PARAMETERS].setChunk(1);
            data[state + _NESTED_PARAMETERS].setChunks(states.size);
            data[state + _NESTED_PARAMETERS].setStaticData(states);
        }
    }

    override fun onSurfaceChanged(gl: GL10, height: Int, width: Int) {
        _width = width.toFloat();
        _height = height.toFloat();
        GLES30.glViewport(0, 0, height, width);
        val data: HYTGLStaticAttribute = _program.getGlobalAttributes()[0];
        data.setStaticData(arrayOf(_height, _width));
    }

    override fun onDrawFrame(gl: GL10) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);
        val data: Array<HYTGLStaticAttribute> = _program.getGlobalAttributes();
        _executionTime += 1.0f / 60.0f;
        data[1].setStaticData(arrayOf(_executionTime));
        _setStates(data);
        _program.use(gl, mapOf(Pair(_canvas) { -> GLES30.glDrawArrays(GLES30.GL_TRIANGLE_FAN, 0, 4) }));
    }


}