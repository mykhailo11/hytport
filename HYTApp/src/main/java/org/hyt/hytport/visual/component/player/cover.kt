package org.hyt.hytport.visual.component.player

import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
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
        if (album != null) {
            Image(
                bitmap = album.asImageBitmap(),
                contentScale = ContentScale.Crop,
                contentDescription = null,
                modifier = Modifier
                    .aspectRatio(1.0f)
                    .scale(0.8f)
                    .border(
                        width = 10.dp,
                        color = colorResource(R.color.hyt_dark),
                        shape = CircleShape
                    )
                    .padding(8.dp)
                    .clip(CircleShape)
            );
        } else {
            Image(
                painter = painterResource(R.drawable.hyt_empty_cover_200dp),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .scale(0.8f)
                    .aspectRatio(1.0f)
                    .border(
                        width = 10.dp,
                        color = colorResource(R.color.hyt_dark),
                        shape = CircleShape
                    )
                    .padding(8.dp)
                    .clip(CircleShape)

            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun coverPreview(){
    cover(null);
}
