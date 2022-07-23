package org.hyt.hytport.visual.component.custom

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import org.hyt.hytport.R
import org.hyt.hytport.visual.component.montserrat

@Composable
fun primaryButton(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = colorResource(R.color.hyt_grey),
    round: Int = 30,
    textColor: Color = colorResource(R.color.hyt_white),
    long: (() -> Unit)? = null,
    click: () -> Unit
) {
    Text(
        text = text,
        textAlign = TextAlign.Center,
        fontFamily = montserrat,
        color = textColor,
        modifier = Modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        long?.invoke()
                    },
                    onTap = {
                        click()
                    }
                )
            }
            .background(
                color = color,
                shape = RoundedCornerShape(round)
            )
            .then(modifier)
    );
}