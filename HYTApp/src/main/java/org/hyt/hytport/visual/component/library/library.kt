package org.hyt.hytport.visual.component.library

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.hyt.hytport.R
import org.hyt.hytport.audio.api.model.HYTAudioManager
import org.hyt.hytport.audio.api.model.HYTAudioModel
import org.hyt.hytport.audio.api.service.HYTBinder
import org.hyt.hytport.visual.component.custom.recycler
import java.util.concurrent.ScheduledExecutorService


@Composable
fun library(
    player: HYTBinder,
    click: (HYTAudioModel) -> Unit,
    back: () -> Unit,
    executor: ScheduledExecutorService
) {
    var currentManager: HYTAudioManager? by remember { mutableStateOf(null) };
    val ready: Boolean by produceState(
        initialValue = false,
        currentManager
    ) {
        currentManager?.queue { items: MutableList<HYTAudioModel> ->
            value = items.isNotEmpty();
        }
    };
    var filter: (HYTAudioModel) -> Boolean by remember { mutableStateOf({ true }) };
    var current: Long by remember { mutableStateOf(-1L); };
    var moving: Long by remember { mutableStateOf(-1L) };
    val auditor: HYTBinder.Companion.HYTAuditor by remember(player) {
        derivedStateOf {
            object : HYTBinder.Companion.HYTAuditor {

                override fun onReady(audio: HYTAudioModel) {
                    player.manger { actualManager: HYTAudioManager ->
                        currentManager = actualManager;
                        currentManager?.current { currentAudio: HYTAudioModel ->
                            current = currentAudio.getId();
                        }
                    }
                }

                override fun onNext(audio: HYTAudioModel) {
                    current = audio.getId();
                }

                override fun onPrevious(audio: HYTAudioModel) {
                    current = audio.getId();
                }

                override fun onSetManager(manager: HYTAudioManager) {
                    currentManager = manager;
                    manager.current { audio: HYTAudioModel ->
                        current = audio.getId();
                    }
                }
            }
        }
    };
    DisposableEffect(player) {
        player.addAuditor(auditor);
        onDispose {
            player.removeAuditor(auditor);
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.hyt_library_close_200dp),
                contentDescription = null,
                modifier = Modifier
                    .height(30.dp)
                    .aspectRatio(1.0f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        back()
                    }
                    .padding(
                        start = 0.dp,
                        top = 0.dp,
                        end = 10.dp,
                        bottom = 0.dp
                    )
            )
            search(
                executor = executor,
                search = { regex: Regex ->
                    filter = { audio: HYTAudioModel ->
                        matches(audio, regex);
                    }
                },
                reset = {
                    filter = { true };
                }
            )
        }
        if (currentManager != null && ready) {
            Row(
                modifier = Modifier
                    .weight(1.0f)
                    .padding(
                        start = 5.dp,
                        end = 5.dp,
                        top = 0.dp,
                        bottom = 10.dp
                    )
            ) {
                recycler(
                    executor = executor,
                    manager = currentManager!!,
                    filter = filter,
                    focus = { movingItem: HYTAudioModel?, _ ->
                        if (movingItem == null) {
                            moving = -1L;
                            false;
                        } else {
                            val id: Long = movingItem.getId();
                            if (moving != id) {
                                moving = id;
                            }
                            true;
                        }
                    },
                    modifier = Modifier
                        .padding(
                            horizontal = 20.dp,
                            vertical = 0.dp
                        )
                ) @Composable { audio: HYTAudioModel ->
                    val focus: Boolean by remember(moving, audio) {
                        derivedStateOf {
                            moving == audio.getId()
                        }
                    }
                    val color: Color by animateColorAsState(
                        if (focus)
                            colorResource(R.color.hyt_accent_grey)
                        else
                            colorResource(R.color.hyt_transparent)
                    );
                    item(
                        executor = executor,
                        empty = painterResource(R.drawable.hyt_empty_cover_200dp),
                        item = audio,
                        current = current == audio.getId(),
                        click = click,
                        modifier = Modifier
                            .background(
                                color = color,
                                shape = remember { RoundedCornerShape(30) }
                            )
                    )
                }
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