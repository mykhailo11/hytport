package org.hyt.hytport.visual.component.custom

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource

@Composable
fun imageButton(
    id: Int,
    modifier: Modifier = Modifier,
    click: () -> Unit
) {
    Image(
        painter = painterResource(id),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = Modifier
            .then(modifier)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = click
            )
    );
}