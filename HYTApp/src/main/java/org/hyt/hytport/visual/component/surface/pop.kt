package org.hyt.hytport.visual.component.surface

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hyt.hytport.R
import org.hyt.hytport.visual.component.custom.primaryButton
import org.hyt.hytport.visual.component.fonts

@Composable
fun pop(
    title: String,
    content: AnnotatedString,
    contentClick: (() -> Unit)?,
    confirm: String,
    accept: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .then(modifier)
            .fillMaxSize()
            .padding(50.dp)
    ) {
        Text(
            textAlign = TextAlign.Center,
            fontFamily = fonts,
            text = title,
            color = colorResource(R.color.hyt_grey),
            fontSize = 35.sp,
        );
        Text(
            textAlign = TextAlign.Center,
            text = content,
            fontFamily = fonts,
            color = colorResource(R.color.hyt_text_dark),
            lineHeight = 30.sp,
            modifier = Modifier
                .padding(
                    horizontal = 0.dp,
                    vertical = 40.dp
                )
                .weight(
                    weight = 1.0f,
                    fill = false
                )
                .verticalScroll(
                    state = rememberScrollState(0)
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    if (contentClick != null) {
                        contentClick();
                    }
                }
        );
        primaryButton(
            text = remember { confirm },
            click = accept
        );
    }
}