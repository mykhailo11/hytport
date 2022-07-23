package org.hyt.hytport.visual.component.library

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import org.hyt.hytport.R
import org.hyt.hytport.visual.api.model.HYTQueueController
import org.hyt.hytport.visual.factory.HYTComponentFactory
import java.util.concurrent.ExecutorService

@Composable
fun rememberQueueController(
    hoverBack: Color = colorResource(R.color.hyt_press),
    currentBack: Color = colorResource(R.color.hyt_accent),
    back: Color = colorResource(R.color.hyt_scroller),
    text: Color = colorResource(R.color.hyt_white),
    currentText: Color = colorResource(R.color.hyt_black),
    executor: ExecutorService
): HYTQueueController {
    val controller: HYTQueueController by remember(
        hoverBack,
        currentBack,
        back,
        text,
        currentText,
        executor
    ) {
        derivedStateOf {
            HYTComponentFactory.getQueueController(
                currentBack = currentBack,
                hoverBack = hoverBack,
                back = back,
                text = text,
                currentText = currentText,
                executor = executor
            );
        }
    }
    return controller;
}