package org.hyt.hytport.util

class HYTMathUtil {

    companion object{
        fun getNormalBytesAverage(bytes: ByteArray): Float{
            var average: Float = 0.0f;
            bytes.forEach {
                average = (average + (it.toFloat() + Byte.MAX_VALUE) * 0.5f / Byte.MAX_VALUE ) * 0.5f
            }
            return average;
        }
    }

}