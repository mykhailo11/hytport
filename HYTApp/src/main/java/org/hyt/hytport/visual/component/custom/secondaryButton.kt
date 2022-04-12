package org.hyt.hytport.visual.component.custom

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.hyt.hytport.R
import org.hyt.hytport.visual.component.fonts

@Composable
fun secondaryButton(
    text: String,
    modifier: Modifier = Modifier,
    click: () -> Unit
) {
    Text(
        text = text,
        fontFamily = fonts,
        color = colorResource(R.color.hyt_accent),
        modifier = Modifier
            .then(modifier)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = click
            )
            .background(
                color = colorResource(R.color.hyt_white),
                shape = RoundedCornerShape(30)
            )
            .height(IntrinsicSize.Min)
            .padding(
                horizontal = 40.dp,
                vertical = 15.dp
            )
    );
}