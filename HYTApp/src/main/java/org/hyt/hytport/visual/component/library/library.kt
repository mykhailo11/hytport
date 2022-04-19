package org.hyt.hytport.visual.component.library

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    val queue: MutableList<HYTAudioModel>? by produceState(
        initialValue = null as MutableList<HYTAudioModel>?,
        currentManager
    ) {
        currentManager?.queue { items: MutableList<HYTAudioModel> ->
            value = items;
        }
    };
    var filter: (HYTAudioModel) -> Boolean by remember { mutableStateOf({ true }) };
    var current: Long by remember { mutableStateOf(-1L); };
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
        if (queue != null && queue!!.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .weight(1.0f)
                    .padding(
                        start = 10.dp,
                        end = 10.dp,
                        top = 0.dp,
                        bottom = 10.dp
                    )
            ) {
                recycler(
                    executor = executor,
                    items = queue!!,
                    filter = filter,
                    modifier = Modifier
                        .padding(
                            horizontal = 20.dp,
                            vertical = 0.dp
                        )
                ) @Composable { audio: HYTAudioModel ->
                    item(
                        executor = executor,
                        empty = painterResource(R.drawable.hyt_empty_cover_200dp),
                        item = audio,
                        current = current == audio.getId(),
                        click = click
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