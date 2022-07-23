package org.hyt.hytport.visual.component.loading

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.hyt.hytport.R

@Composable
fun loadingIcon(){
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    Pair(0.01f, colorResource(R.color.hyt_accent_grey)),
                    Pair(1.0f, colorResource(R.color.hyt_dark))
                )
            )
    ) {
        Image(
            painter = painterResource(R.drawable.hyt_player_icon_200dp),
            contentDescription = null,
            modifier = Modifier
                .requiredSizeIn(
                    minWidth = 0.dp,
                    minHeight = 0.dp,
                    maxWidth = 300.dp,
                    maxHeight = 300.dp
                )
        );
    }
}