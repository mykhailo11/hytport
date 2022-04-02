package org.hyt.hytport.visual.component.player

import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.hyt.hytport.R

@Composable
fun cover(
    album: Bitmap?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .then(modifier)
    ) {
        Image(
            painter = painterResource(
                if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE)
                    R.drawable.hyt_cover_wrapper_landscape_200dp
                else
                    R.drawable.hyt_cover_wrapper_200dp
            ),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .aspectRatio(1.0f)
        );
        val borderColor: Color = Color(11, 11, 19, 255);
        if (album != null) {
            Image(
                bitmap = album.asImageBitmap(),
                contentScale = ContentScale.Crop,
                contentDescription = null,
                modifier = Modifier
                    .aspectRatio(1.0f)
                    .scale(0.8f)
                    .clip(CircleShape)
                    .border(
                        width = 10.dp,
                        color = borderColor,
                        shape = CircleShape
                    )
            );
        } else {
            Image(
                painter = painterResource(R.drawable.hyt_empty_cover_200dp),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .scale(0.8f)
                    .aspectRatio(1.0f)
                    .clip(CircleShape)
                    .border(
                        width = 10.dp,
                        color = borderColor,
                        shape = CircleShape
                    )

            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun coverPreview(){
    cover(null);
}
