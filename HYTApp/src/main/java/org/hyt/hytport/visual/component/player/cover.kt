package org.hyt.hytport.visual.component.player

import android.graphics.Bitmap
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
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
            .sizeIn(
                minWidth = 0.dp,
                minHeight = 0.dp,
                maxHeight = 250.dp,
                maxWidth = 250.dp
            )
            .then(modifier)
    ) {
        val coverModifier: Modifier = Modifier
            .aspectRatio(1.0f)
        if (album != null) {
            Image(
                bitmap = album.asImageBitmap(),
                contentScale = ContentScale.Crop,
                contentDescription = null,
                modifier = coverModifier
                    .border(
                        width = 3.dp,
                        color = colorResource(R.color.hyt_accent_grey),
                        shape = RoundedCornerShape(coverAnimation)
                    )
                    .padding(2.dp)
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
                        width = 3.dp,
                        color = colorResource(R.color.hyt_accent_grey),
                        shape = CircleShape
                    )
                    .padding(2.dp)
                    .clip(CircleShape)
            )
        }
    }
}
