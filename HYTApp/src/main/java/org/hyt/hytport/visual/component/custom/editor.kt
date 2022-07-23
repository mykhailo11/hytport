package org.hyt.hytport.visual.component.custom

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.hyt.hytport.R
import org.hyt.hytport.visual.api.model.HYTEditorState
import org.hyt.hytport.visual.component.montserrat

@Composable
fun editor(
    state: HYTEditorState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .border(
                width = 1.0.dp,
                color = colorResource(R.color.hyt_grey),
                shape = remember { RoundedCornerShape(30) }
            )
            .clip(
                shape = remember { RoundedCornerShape(30) }
            )
    ) {
        Text(
            text = state.label(),
            fontFamily = montserrat,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.hyt_white),
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    state.click();
                }
                .padding(
                    horizontal = 15.dp,
                    vertical = 15.dp
                )
        );
        BasicTextField(
            enabled = state.enabled(),
            value = state.value(),
            onValueChange = { new: String ->
                state.value(new);
            },
            textStyle = TextStyle(
                color = colorResource(R.color.hyt_white),
                fontFamily = montserrat,
            ),
            cursorBrush = SolidColor(colorResource(R.color.hyt_text_dark)),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 5.dp,
                    vertical = 15.dp
                )
        );
    }
}