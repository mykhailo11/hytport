package org.hyt.hytport.visual.component.library

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import org.hyt.hytport.visual.api.model.HYTItemController
import org.hyt.hytport.visual.factory.HYTComponentFactory
import java.util.concurrent.ExecutorService

@Composable
fun rememberItemController(
    executor: ExecutorService
): HYTItemController {
    val state: HYTItemController by remember(executor) {
        derivedStateOf {
            HYTComponentFactory.getItemController(executor);
        }
    };
    return state;
}