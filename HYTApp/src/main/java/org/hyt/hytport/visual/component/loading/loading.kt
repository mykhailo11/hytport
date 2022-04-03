package org.hyt.hytport.visual.component.loading

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import org.hyt.hytport.R

@Composable
fun loadingIcon(){
    Image(
        painter = painterResource(R.drawable.hyt_player_icon_200dp),
        contentDescription = null,
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    Pair(0.0f, colorResource(R.color.hyt_accent_dark)),
                    Pair(1.0f, colorResource(R.color.hyt_dark))
                )
            )
    );
}