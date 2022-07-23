package org.hyt.hytport.util

import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.Size
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class HYTUtil {

    companion object{


        fun readSource(path: String, assets: AssetManager): String{
            return assets.open(path).bufferedReader().use {
                it.readLines().reduce() { accumulator: String, current: String ->
                    accumulator + "\n" + current
                }
            }
        }

        fun readByteSource(path: String, assets: AssetManager): ByteBuffer {
            return assets.open(path).use {
                val bytes: ByteArray = ByteArray(it.available());
                it.read(bytes);
                it.close();
                ByteBuffer.allocateDirect(bytes.size)
                    .order(ByteOrder.nativeOrder())
                    .put(bytes);
            }
        }

        fun getBitmap(path: Uri?, resolver: ContentResolver): Bitmap?{
            if (path == null){
                return null;
            }
            return try{
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P){
                    resolver.loadThumbnail(path, Size(800, 800), null);
                } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P){
                    val source = ImageDecoder.createSource(resolver, path);
                    return ImageDecoder.decodeBitmap(source);
                } else {
                    val stream: InputStream? = resolver.openInputStream(path);
                    return BitmapFactory.decodeStream(stream);
                }
            }catch (exception: Exception){
                null;
            }
        }

        fun wrapIntentForService(context: Context, intent: Intent): PendingIntent{
            val pending: PendingIntent = PendingIntent.getService(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            );
            return pending;
        }

        fun anyMatch(base: String): String {
            return base.lowercase().map {
                ".*[$it${it.uppercase()}]"
            }.reduce { result: String, current: String ->
                result + current
            } + ".*";
        }

        fun formatTime(time: Float): String {
            val minutes: Int = (time / 60.0f).toInt().coerceIn(0, 99);
            val seconds: Int = (time.toInt() % 60).coerceIn(0, 59);
            val minutesPart: String = when {
                minutes < 10 -> "0$minutes";
                else -> "$minutes"
            };
            val secondsPart: String = when {
                seconds < 10 -> "0$seconds";
                else -> "$seconds";
            };
            return "$minutesPart:$secondsPart";
        }

    }

}