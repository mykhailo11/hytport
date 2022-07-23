package org.hyt.hytport.visual.component.surface

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hyt.hytport.R
import org.hyt.hytport.visual.api.model.HYTEditorState
import org.hyt.hytport.visual.api.model.HYTFormState
import org.hyt.hytport.visual.component.custom.editor
import org.hyt.hytport.visual.component.custom.primaryButton
import org.hyt.hytport.visual.component.montserrat

@Composable
fun form(
    editorState: HYTEditorState,
    state: HYTFormState,
    modifier: Modifier = Modifier
) {
    val stateEffect: HYTFormState by rememberUpdatedState(state);
    DisposableEffect(editorState) {
        val auditor: HYTEditorState.Companion.HYTAuditor = object :
            HYTEditorState.Companion.HYTAuditor {

            override fun onChange(value: String) {
                stateEffect.value(value);
            }

        };
        editorState.addAuditor(auditor)
        onDispose {
            editorState.removeAuditor(auditor);
        }
    }
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .sizeIn(
                maxWidth = 300.dp
            )
            .then(modifier)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(30.dp)
    ) {
        Text(
            textAlign = TextAlign.Center,
            fontFamily = montserrat,
            fontWeight = FontWeight.Light,
            text = state.title(),
            color = colorResource(R.color.hyt_accent),
            fontSize = 35.sp,
        );
        editor(
            editorState,
            modifier = Modifier
                .then(modifier)
                .padding(
                    horizontal = 0.dp,
                    vertical = 40.dp
                )
                .weight(
                    weight = 1.0f,
                    fill = false
                )
        );
        Column(
            verticalArrangement = Arrangement.spacedBy(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                primaryButton(
                    text = state.confirm(),
                    click = {
                        state.accept();
                    },
                    color = colorResource(R.color.hyt_accent),
                    textColor = colorResource(R.color.hyt_black),
                    modifier = Modifier
                        .padding(
                            horizontal = 20.dp,
                            vertical = 15.dp
                        )
                        .weight(0.6f)
                );
                primaryButton(
                    text = remember { "Cancel" },
                    click = {
                        state.cancel();
                    },
                    color = colorResource(R.color.hyt_accent_dark),
                    modifier = Modifier
                        .padding(
                            horizontal = 20.dp,
                            vertical = 15.dp
                        )
                        .weight(0.4f)
                )
            }
            for (action: String in state.actions().keys) {
                primaryButton(
                    text = action,
                    click = {
                        state.action(action);
                    },
                    modifier = Modifier
                        .padding(
                            horizontal = 20.dp,
                            vertical = 15.dp
                        )
                        .fillMaxWidth()
                )
            }
        }
    }
}