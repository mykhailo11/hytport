package org.hyt.hytport.neural.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import org.hyt.hytport.util.HYTUtil
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class HYTNeuralService: Service() {

    companion object {

        public interface HYTBinder {

            fun predict(data: Array<Float>): Array<Float>;

        }

        private val _PATH: String = "model.tflite";

    }

    private var _lite: Interpreter? = null;

    private var _binder: IBinder? = null;

    override fun onCreate() {
        _binder = object: Binder(), HYTBinder {

            override fun predict(data: Array<Float>): Array<Float> {
                val output: FloatBuffer = FloatBuffer.allocate(Float.SIZE_BYTES * 20);
                val input: ByteBuffer = ByteBuffer.allocateDirect(Float.SIZE_BYTES * 1024)
                    .order(ByteOrder.nativeOrder());
                data.forEach { value: Float ->
                    input.putFloat(value);
                }
                _lite?.run(input, output);
                val array: FloatArray = output.array();
                return arrayOf(array.indices.maxByOrNull { index: Int ->
                    array[index];
                }?.toFloat()?.div(20.0f) ?: 0.0f);
            }

        };
        _lite = Interpreter(
            HYTUtil.readByteSource(_PATH, assets)
        );
        super.onCreate()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return _binder;
    }

    override fun onDestroy() {
        _lite?.close();
        super.onDestroy()
    }
}