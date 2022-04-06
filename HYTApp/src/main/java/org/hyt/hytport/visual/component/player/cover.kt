package org.hyt.hytport.visual.component.player

import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.hyt.hytport.R

@Composable
fun cover(
    album: Bitmap?,
    modifier: Modifier = Modifier
) {
    var show: Boolean by remember { mutableStateOf(false) };
    val coverAnimation: Int by animateIntAsState(if (show) 0 else 50);
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
        val coverModifier: Modifier = Modifier
            .aspectRatio(1.0f)
            .scale(0.8f)
        if (album != null) {
            Image(
                bitmap = album.asImageBitmap(),
                contentScale = ContentScale.Crop,
                contentDescription = null,
                modifier = coverModifier
                    .border(
                        width = 4.dp,
                        color = colorResource(R.color.hyt_dark),
                        shape = RoundedCornerShape(coverAnimation)
                    )
                    .padding(3.dp)
                    .clip(RoundedCornerShape(coverAnimation))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ){
                        show = !show;
                    }
            );
        } else {
            Image(
                painter = painterResource(R.drawable.hyt_empty_cover_200dp),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = coverModifier
                    .border(
                        width = 4.dp,
                        color = colorResource(R.color.hyt_dark),
                        shape = CircleShape
                    )
                    .padding(3.dp)
                    .clip(CircleShape)
            )
        }
    }
}
