package org.hyt.hytport.visual.component.surface

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hyt.hytport.R
import org.hyt.hytport.visual.api.model.HYTPopState
import org.hyt.hytport.visual.component.custom.primaryButton
import org.hyt.hytport.visual.component.montserrat

@Composable
fun pop(
    state: HYTPopState,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = Modifier
            .then(modifier)
            .fillMaxSize()
            .padding(50.dp)

    ) {
        val width: Float = maxWidth.value;
        val height: Float = maxHeight.value;
        val image: Painter? = state.image();
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1.0f)
            ) {
                Text(
                    textAlign = TextAlign.Center,
                    fontFamily = montserrat,
                    fontWeight = FontWeight.Light,
                    text = state.title(),
                    color = colorResource(R.color.hyt_accent),
                    fontSize = 35.sp,
                );
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(
                            horizontal = 0.dp,
                            vertical = 40.dp
                        )
                        .weight(
                            weight = 1.0f,
                            fill = false
                        )
                ) {
                    Text(
                        textAlign = TextAlign.Center,
                        text = state.content(),
                        fontFamily = montserrat,
                        color = colorResource(R.color.hyt_text_dark),
                        lineHeight = 30.sp,
                        modifier = Modifier
                            .padding(10.dp)
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
                                state.click();
                            }
                    );
                    if (image != null && height > 500 && width > 200) {
                        Image(
                            painter = image,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(10.dp)
                        );
                    }
                }
                primaryButton(
                    text = state.confirm(),
                    click = {
                        state.accept();
                    },
                    modifier = Modifier
                        .padding(
                            horizontal = 40.dp,
                            vertical = 15.dp
                        )
                );
            }
            if (image != null && height <= 500 && width > 500) {
                Image(
                    painter = image,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(10.dp)
                        .weight(1.0f)
                );
            }
        }
    }
}