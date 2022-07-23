package org.hyt.hytport.util

class HYTMathUtil {

    companion object{

        fun getNormalBytesAverage(bytes: ByteArray): Float{
            var average: Float = 0.0f;
            bytes.forEach {
                average = (average + getNormalByte(it.toFloat())) * 0.5f
            }
            return average;
        }

        fun getNormalBytesAverage(bytes: Array<Float>): Float {
            return bytes.reduce { average, value ->
                (average + value) * 0.5f;
            };
        }

        fun getNormalByteArray(bytes: ByteArray): Array<Float> {
            return bytes.map { byte: Byte ->
                getNormalByte(byte.toFloat());
            }.toTypedArray();
        }

        fun getNormalByte(byte: Float): Float{
            return (byte + Byte.MAX_VALUE) * 0.5f / Byte.MAX_VALUE;
        }

    }

}