package org.hyt.hytport.visual.component.library

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.hyt.hytport.R
import org.hyt.hytport.audio.api.model.HYTAudioManager
import org.hyt.hytport.audio.api.model.HYTAudioModel
import org.hyt.hytport.audio.api.service.HYTBinder
import org.hyt.hytport.audio.api.service.HYTQueueProvider
import org.hyt.hytport.visual.api.model.HYTClipState
import org.hyt.hytport.visual.api.model.HYTHoverState
import org.hyt.hytport.visual.api.model.HYTItemController
import org.hyt.hytport.visual.api.model.HYTQueueController
import org.hyt.hytport.visual.component.custom.clip
import org.hyt.hytport.visual.component.custom.hoverable
import org.hyt.hytport.visual.component.custom.rememberClipState
import org.hyt.hytport.visual.component.custom.rememberHoverState
import org.hyt.hytport.visual.component.montserrat

@Composable
fun catalogue(
    player: HYTBinder,
    controller: HYTItemController,
    queueController: HYTQueueController,
    modifier: Modifier = Modifier
) {
    val process: CoroutineScope = rememberCoroutineScope();
    val mainstream: String = stringResource(R.string.hyt_mainstream);
    val hidden: String = stringResource(R.string.hyt_hidden);
    var startHover: Boolean by remember { mutableStateOf(false) };
    var newHover: Boolean by remember { mutableStateOf(false) };
    val newColor: Color by animateColorAsState(
        if (newHover) colorResource(R.color.hyt_accent_dark)
        else colorResource(R.color.hyt_accent_grey)
    );
    val newWeight: Float by animateFloatAsState(
        if (startHover) 1.0f
        else 0.0f
    );
    val list: MutableList<String> = remember { mutableStateListOf() };
    LaunchedEffect(player) {
        player.provider { provider: HYTQueueProvider ->
            process.launch {
                provider.getAll { items: List<String> ->
                    list.clear();
                    list.addAll(
                        items.filter { item: String ->
                            item != mainstream && item != hidden
                        }
                    );
                }
            }
        }
    }
    val queues: MutableList<String>? by produceState(
        initialValue = null as MutableList<String>?,
        player
    ) {
        player.provider { provider: HYTQueueProvider ->
            process.launch {
                provider.getAll { items: List<String> ->
                    if (value == null) {
                        value = ArrayList();
                    }
                    value?.clear();
                    value?.addAll(
                        items.filter { item: String ->
                            item != mainstream && item != hidden
                        }
                    );
                }
            }
        }
    };
    val clipState: HYTClipState = rememberClipState();
    LaunchedEffect(clipState) {
        clipState.expand(2.0f);
    }
    val startHoverEffect: Boolean by rememberUpdatedState(startHover);
    val queuesEffect: (MutableList<String>)? by rememberUpdatedState(list);
    val playerEffect: HYTBinder by rememberUpdatedState(player);
    val queueControllerEffect: HYTQueueController by rememberUpdatedState(queueController);
    DisposableEffect(controller) {
        val auditor: HYTItemController.Companion.HYTAuditor = object :
            HYTItemController.Companion.HYTAuditor {

            private var _selected: HYTAudioModel? = null;

            override fun onMove(start: Float, top: Float, width: Float, height: Float) {
                if (controller.selected() != null) {
                    clipState.round(0.3f);
                    clipState.offset(start, top);
                    clipState.size(width, height);
                }
            }

            override fun onSelect(audio: HYTAudioModel?): Boolean {
                _selected = audio;
                val selected: HYTAudioModel? = controller.selected();
                if (audio == null && newHover) {
                    queueControllerEffect.add { new: String ->
                        if (new.isNotBlank()) {
                            playerEffect.provider { provider: HYTQueueProvider ->
                                process.launch {
                                    selected?.let { item: HYTAudioModel ->
                                        process.launch {
                                            provider.add(new, item) {
                                                queuesEffect?.add(new);
                                                queueController.click(new);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (_selected == null && startHoverEffect) {
                    startHover = false;
                }
                return super.onSelect(audio);
            }

            override fun onFocus(audio: HYTAudioModel?, move: Boolean): Boolean {
                if (move && _selected != null && !startHoverEffect) {
                    startHover = true;
                }
                return super.onFocus(audio, move);
            }

        }
        controller.addAuditor(auditor);
        onDispose {
            controller.removeAuditor(auditor);
        }
    }
    DisposableEffect(queueController) {
        val auditor: HYTQueueController.Companion.HYTAuditor = object :
            HYTQueueController.Companion.HYTAuditor {

            override fun onClick(queue: String) {
                val current: String? = queueController.current();
                if (current == null || queue != current) {
                    playerEffect.provider { provider: HYTQueueProvider ->
                        process.launch {
                            if (current != null) {
                                playerEffect.save();
                            }
                            provider.getByName(queue) { manager: HYTAudioManager ->
                                playerEffect.setManager(manager);
                                queueController.click(manager.name())
                            }
                        }
                    }
                }
            }

            override fun onLong(queue: String) {
                if (queue != mainstream && queue != hidden) {
                    val current: String? = queueController.current();
                    queueController.edit(
                        queue = queue,
                        save = { item: String ->
                            if (item != queue && item.isNotBlank()) {
                                playerEffect.provider { provider: HYTQueueProvider ->
                                    process.launch {
                                        provider.edit(queue, item) { saved: Boolean ->
                                            if (saved) {
                                                queuesEffect?.add(queuesEffect!!.indexOf(queue), item);
                                            }
                                        };
                                    }
                                }
                            }
                        },
                        remove = {
                            playerEffect.provider { provider: HYTQueueProvider ->
                                process.launch {
                                    provider.remove(queue) {
                                        if (queue == current) {
                                            process.launch {
                                                provider
                                                    .getByName(
                                                        mainstream
                                                    ) { manager: HYTAudioManager ->
                                                        playerEffect.setManager(manager);
                                                        queueController.click(manager.name());
                                                    }
                                            }
                                        }
                                        queuesEffect?.remove(queue);
                                    }
                                }
                            }
                        }
                    )
                }
            }

        }
        queueController.addAuditor(auditor);
        onDispose {
            queueController.removeAuditor(auditor);
        }
    }
    val newHoverState: HYTHoverState = rememberHoverState();
    DisposableEffect(newHoverState) {
        val auditor: HYTHoverState.Companion.HYTAuditor = object :
            HYTHoverState.Companion.HYTAuditor {

            override fun onHover() {
                newHover = true;
            }

            override fun onOut() {
                newHover = false;
            }

        }
        newHoverState.addAuditor(auditor);
        onDispose {
            newHoverState.removeAuditor(auditor);
        }
    }
    LaunchedEffect(player, queueController) {
        player.manager { manager: HYTAudioManager ->
            queueController.click(manager.name());
        }
    }
    val queuesState: LazyListState = rememberLazyListState();
    var region: Int by remember { mutableStateOf(-1) };
    clip(
        state = clipState,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(
                translationY = if (newWeight < 0.01f) region.toFloat() else 0.0f
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            hoverable(
                state = newHoverState,
                controller = controller,
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coordinates: LayoutCoordinates ->
                        region = coordinates.size.height;
                    }
                    .graphicsLayer(
                        scaleY = newWeight,
                        transformOrigin = TransformOrigin(
                            pivotFractionY = 1.0f,
                            pivotFractionX = 0.5f
                        )
                    )
            ) @Composable {
                Text(
                    text = remember { "New" },
                    color = colorResource(R.color.hyt_grey),
                    fontFamily = montserrat,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = newColor
                        )
                        .padding(
                            15.dp
                        )
                )
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer(
                        translationY = if (newWeight < 0.01f) -region.toFloat() else 0.0f
                    )
                    .then(modifier)
            ) {
                LazyRow(
                    state = queuesState,
                    contentPadding = remember {
                        PaddingValues(
                            horizontal = 10.dp,
                            vertical = 0.dp
                        )
                    },
                    horizontalArrangement = Arrangement.spacedBy(15.dp),
                    modifier = Modifier
                        .padding(
                            horizontal = 0.dp,
                            vertical = 12.dp
                        )
                ) {
                    item {
                        queue(
                            item = mainstream,
                            queueController = queueController,
                            controller = controller
                        )
                    }
                    item {
                        queue(
                            item = hidden,
                            queueController = queueController,
                            controller = controller,
                        )
                    }
                    items(
                        items = list,
                        key = { item: String ->
                            item;
                        }
                    ) { item: String ->
                        queue(
                            item = item,
                            controller = controller,
                            queueController = queueController
                        )
                    }
                }
            }
        }
    }
}