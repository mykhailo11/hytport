package org.hyt.hytport.visual.component.library

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.hyt.hytport.R
import org.hyt.hytport.audio.api.model.HYTAudioManager
import org.hyt.hytport.audio.api.model.HYTAudioModel
import org.hyt.hytport.audio.api.service.HYTBinder
import org.hyt.hytport.audio.api.service.HYTQueueProvider
import org.hyt.hytport.util.HYTUtil
import org.hyt.hytport.visual.api.model.HYTEditorState
import org.hyt.hytport.visual.api.model.HYTFormState
import org.hyt.hytport.visual.api.model.HYTItemController
import org.hyt.hytport.visual.api.model.HYTQueueController
import org.hyt.hytport.visual.component.custom.editor
import org.hyt.hytport.visual.component.custom.recycler
import org.hyt.hytport.visual.component.custom.rememberEditorState
import org.hyt.hytport.visual.component.montserrat
import org.hyt.hytport.visual.component.surface.form
import org.hyt.hytport.visual.component.surface.rememberFormState
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

@Composable
fun library(
    player: HYTBinder,
    click: (HYTAudioModel) -> Unit,
    back: () -> Unit,
    executor: ScheduledExecutorService,
    modifier: Modifier = Modifier
) {
    val mainstream: String = stringResource(R.string.hyt_mainstream);
    val hidden: String = stringResource(R.string.hyt_hidden);
    var currentManager: HYTAudioManager? by remember { mutableStateOf(null) };
    LaunchedEffect(player) {
        player.manager { manager: HYTAudioManager ->
            currentManager = manager;
        }
    }
    var show: Boolean by remember { mutableStateOf(false) };
    val formOpacity by animateFloatAsState(if (show) 1.0f else 0.0f);
    val process: CoroutineScope = rememberCoroutineScope();
    val density: Density = LocalDensity.current;
    var current: Long by remember { mutableStateOf(-1L); };
    val controller: HYTItemController = rememberItemController(
        executor = executor
    );
    val queueController: HYTQueueController = rememberQueueController(
        executor = executor
    );
    val searchState: HYTEditorState = rememberEditorState(
        remember { "" },
        remember { "Search" }
    );
    val formEditorState: HYTEditorState = rememberEditorState(
        remember { "" },
        remember { "Name" }
    );
    val formState: HYTFormState = rememberFormState(
        remember { "New Queue" },
        remember { "Save" }
    )
    val currentManagerEffect: HYTAudioManager? by rememberUpdatedState(currentManager);
    val playerEffect: HYTBinder by rememberUpdatedState(player);
    val queueControllerEffect: HYTQueueController by rememberUpdatedState(queueController);
    DisposableEffect(
        controller
    ) {
        val auditor: HYTItemController.Companion.HYTAuditor = object :
            HYTItemController.Companion.HYTAuditor {

            override fun onRemove(audio: HYTAudioModel) {
                when (currentManagerEffect?.name()) {
                    mainstream -> _moveToQueue(audio, hidden)
                    hidden -> _moveToQueue(audio, mainstream)
                }
            }

            override fun onFocus(audio: HYTAudioModel?, move: Boolean): Boolean {
                val selected: HYTAudioModel? = controller.selected();
                val hovering: String? = queueControllerEffect.hover();
                if (
                    selected != null
                    && ((hovering == mainstream && currentManagerEffect?.name() == hidden)
                            || (hovering == hidden && currentManagerEffect?.name() == mainstream))
                    && audio == null
                ) {
                    controller.remove(selected);
                    controller.select(null);
                } else if (selected != null && hovering != null && audio == null) {
                    _moveToQueue(selected, hovering);
                    controller.select(null);
                }
                if (audio == null && selected != null) {
                    controller.select(null);
                    controller.move(0.0f, 0.0f, 0.0f, 0.0f);
                }
                return move && selected == null;
            }

            private fun _moveToQueue(audio: HYTAudioModel, to: String) {
                val currentName: String? = currentManagerEffect?.name();
                val self: Boolean = to == currentName;
                val fromMainstreamToHidden: Boolean = currentName == mainstream
                        && to == hidden;
                val toMainstreamFromHidden: Boolean = currentName == hidden
                        && to == mainstream;
                val mainTransition: Boolean = fromMainstreamToHidden || toMainstreamFromHidden;
                playerEffect.provider { provider: HYTQueueProvider ->
                    process.launch {
                        if (!self && (mainTransition || (to != mainstream && to != hidden))) {
                            queueControllerEffect.consume(to);
                            provider.add(
                                to,
                                audio
                            );
                        }
                    }
                }
            }

        };
        controller.addAuditor(auditor);
        onDispose {
            controller.removeAuditor(auditor);
        }
    }
    LaunchedEffect(formState, formOpacity) {
        if (formOpacity < 0.01f) {
            formState.removeAction("Remove");
        }
    }
    DisposableEffect(formState) {
        val auditor: HYTFormState.Companion.HYTAuditor = object :
            HYTFormState.Companion.HYTAuditor {

            override fun onAccept(value: String) {
                show = false;
            }

            override fun onCancel(value: String) {
                show = false;
            }

        }
        formState.addAuditor(auditor);
        onDispose {
            formState.removeAuditor(auditor);
        }
    }
    DisposableEffect(
        searchState
    ) {
        var scheduled: ScheduledFuture<*>? = null;
        val auditor: HYTEditorState.Companion.HYTAuditor = object :
            HYTEditorState.Companion.HYTAuditor {

            override fun onChange(value: String) {
                scheduled?.cancel(true);
                scheduled = executor.schedule(
                    {
                        if (value.isNotBlank() && value.matches("[A-Za-z0-9]*".toRegex())) {
                            controller.filter { audio: HYTAudioModel ->
                                matches(audio, HYTUtil.anyMatch(value).toRegex());
                            }
                        } else {
                            controller.filter(null);
                        }
                    }, 500, TimeUnit.MILLISECONDS
                );
            }

        }
        searchState.addAuditor(auditor);
        onDispose {
            scheduled?.cancel(true);
            searchState.removeAuditor(auditor);
        }
    }
    DisposableEffect(player) {
        val auditor: HYTBinder.Companion.HYTAuditor = object :
            HYTBinder.Companion.HYTAuditor {

            override fun onReady(audio: HYTAudioModel?, time: Long) {
                if (audio != null) {
                    current = audio.getId();
                }
            }

            override fun onNext(audio: HYTAudioModel) {
                current = audio.getId();
            }

            override fun onPrevious(audio: HYTAudioModel) {
                current = audio.getId();
            }

            override fun onSetManager(manager: HYTAudioManager, audio: HYTAudioModel?) {
                currentManager = manager;
                if (audio != null) {
                    current = audio.getId();
                }
            }

        }
        player.addAuditor(auditor);
        onDispose {
            player.removeAuditor(auditor);
        }
    }
    val showEffect: Boolean by rememberUpdatedState(show);
    val formStateEffect: HYTFormState by rememberUpdatedState(formState);
    val formEditorStateEffect: HYTEditorState by rememberUpdatedState(formEditorState);
    DisposableEffect(queueController) {
        val auditor: HYTQueueController.Companion.HYTAuditor = object :
            HYTQueueController.Companion.HYTAuditor {

            override fun onEdit(queue: String, save: ((String) -> Unit)?, remove: (() -> Unit)?) {
                if (!showEffect) {
                    show = true;
                    formEditorStateEffect.enabled(true);
                    formStateEffect.title("Edit Queue");
                    formEditorStateEffect.value(queue);
                    formStateEffect.consumer(save);
                    formState.addAction("Remove") {
                        show = false;
                        remove?.invoke();
                    }
                }
            }

            override fun onAdd(request: ((String) -> Unit)?) {
                if (!showEffect) {
                    show = true;
                    formEditorState.enabled(true);
                    formState.title("New Queue");
                    formEditorState.value("");
                    formStateEffect.consumer(request);
                }
            }

        };
        queueController.addAuditor(auditor);
        onDispose {
            queueController.removeAuditor(auditor);
        }
    }
    var padding: Offset by remember { mutableStateOf(Offset(0.0f, 0.0f)) };
    var size: IntSize by remember { mutableStateOf(IntSize.Zero) };
    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates: LayoutCoordinates ->
                size = coordinates.size;
            }
            .then(modifier)
    ) {
        form(
            editorState = formEditorState,
            state = formState,
            modifier = Modifier
                .graphicsLayer(
                    translationX = if (formOpacity < 0.01f) size.width.toFloat() else 0.0f,
                    alpha = formOpacity
                )
                .zIndex(20.0f)
        )
        recycler(
            executor = executor,
            manager = currentManager,
            controller = controller,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    translationX = if (formOpacity > 0.01f) size.width.toFloat() else 0.0f
                )
                .zIndex(9.0f)
                .padding(
                    horizontal = 15.dp,
                    vertical = 0.dp
                ),
            offset = padding
        ) @Composable { audio: HYTAudioModel ->
            val id: Long by remember(audio) {
                derivedStateOf {
                    audio.getId()
                }
            };
            item(
                executor = executor,
                empty = painterResource(R.drawable.hyt_empty_cover_200dp),
                item = audio,
                current = current == id,
                controller = controller,
                click = click,
                modifier = Modifier
                    .zIndex(10.0f)
            )
        }
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .alpha(1.0f - formOpacity)
                .zIndex(10.0f)
        ) {
            val height: Float by remember(maxHeight) {
                derivedStateOf {
                    with(density) {
                        maxHeight.toPx()
                    }
                }
            };
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = colorResource(R.color.hyt_semi_dark)
                        )
                        .onGloballyPositioned { coordinates: LayoutCoordinates ->
                            padding = Offset(
                                (coordinates.size.height.toFloat()),
                                padding.y
                            );
                        }
                ) {
                    var searchShow: Boolean by remember { mutableStateOf(false) };
                    val searchShowAnimation: Float by animateFloatAsState(
                        if (searchShow) 1.0f
                        else 0.0f
                    )
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 20.dp,
                                vertical = 20.dp
                            )
                    ) {
                        Image(
                            painter = painterResource(R.drawable.hyt_library_close_200dp),
                            contentDescription = null,
                            modifier = Modifier
                                .height(16.dp)
                                .aspectRatio(1.0f)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    back()
                                }
                        )
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1.0f)
                        ) {
                            Text(
                                textAlign = TextAlign.Center,
                                fontFamily = montserrat,
                                text = remember { "Library" },
                                color = colorResource(R.color.hyt_text_dark),
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .graphicsLayer(
                                        alpha = 1.0f - searchShowAnimation,
                                    )
                            );
                            editor(
                                searchState,
                                modifier = Modifier
                                    .graphicsLayer(
                                        scaleX = 0.5f + 0.5f * searchShowAnimation,
                                        scaleY = 0.5f + 0.5f * searchShowAnimation,
                                        alpha = searchShowAnimation,
                                    )
                                    .padding(
                                        horizontal = 10.dp,
                                        vertical = 0.dp
                                    )
                                    .animateContentSize()
                            );
                        }
                        Image(
                            painter = painterResource(
                                if (searchShow) {
                                    R.drawable.hyt_search_close_200dp
                                } else {
                                    R.drawable.hyt_search_200dp
                                }
                            ),
                            contentDescription = null,
                            modifier = Modifier
                                .height(20.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    searchShow = !searchShow;
                                    if (!searchShow) {
                                        searchState.value("");
                                        searchState.enabled(false);
                                        controller.filter(null);
                                    } else {
                                        searchState.enabled(true);
                                    }
                                }
                        )
                    }
                }
                catalogue(
                    player = player,
                    modifier = Modifier
                        .background(
                            color = colorResource(R.color.hyt_semi_dark)
                        )
                        .onGloballyPositioned { coordinates: LayoutCoordinates ->
                            padding = Offset(
                                padding.x,
                                coordinates.size.height.toFloat()
                            );
                        },
                    controller = controller,
                    queueController = queueController
                );
            }
        }
    }
}

fun matches(audio: HYTAudioModel, filter: Regex): Boolean {
    val title: String? = audio.getTitle();
    val artist: String? = audio.getArtist();
    return (title != null && title.matches(filter))
            || (artist != null && artist.matches(filter));
}