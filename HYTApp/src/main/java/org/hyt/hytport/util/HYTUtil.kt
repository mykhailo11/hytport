package org.hyt.hytport.util

import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Size

class HYTUtil {

    companion object{


        fun readSource(path: String, assets: AssetManager): String{
            return assets.open(path).bufferedReader().use {
                it.readLines().reduce() { accumulator: String, current: String ->
                    accumulator + "\n" + current
                }
            }
        }

        fun getBitmap(path: Uri?, resolver: ContentResolver): Bitmap?{
            if (path == null){
                return null;
            }
            return try{
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P){
                    resolver.loadThumbnail(path, Size(800, 800), null);
                }else{
                    return null;
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



    }

}