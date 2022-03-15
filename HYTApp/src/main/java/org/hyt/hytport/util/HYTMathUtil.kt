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

        fun getNormalByte(byte: Float): Float{
            return (byte + Byte.MAX_VALUE) * 0.5f / Byte.MAX_VALUE;
        }
    }

}