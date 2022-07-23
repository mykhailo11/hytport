package org.hyt.hytport.visual.component.custom

import android.content.Context
import android.view.ViewGroup
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.hyt.hytport.visual.api.model.HYTClipState
import org.hyt.hytport.visual.model.HYTClip

@Composable
fun clip(
    state: HYTClipState,
    modifier: Modifier = Modifier,
    body: @Composable () -> Unit
) {
    var view: HYTClip? by remember { mutableStateOf(null) };
    val viewEffect: HYTClip? by rememberUpdatedState(view);
    var expandRegion: Float by remember { mutableStateOf(0.0f) };
    DisposableEffect(state) {
        val auditor: HYTClipState.Companion.HYTAuditor = object:
            HYTClipState.Companion.HYTAuditor {

            override fun onSize(width: Float, height: Float) {
                viewEffect?.setSize(width, height);
                viewEffect?.postInvalidate();
            }

            override fun onOffset(start: Float, top: Float) {
                viewEffect?.setOffset(start, top);
                viewEffect?.postInvalidate();
            }

            override fun onRound(round: Float) {
                viewEffect?.setRound(round);
                viewEffect?.postInvalidate();
            }

            override fun onExpand(expand: Float) {
                expandRegion = expand;
                viewEffect?.expand(expand);
                viewEffect?.postInvalidate();
            }
        }
        state.addAuditor(auditor);
        onDispose {
            state.removeAuditor(auditor);
        }
    }
    LaunchedEffect(view, expandRegion) {
        view?.expand(expandRegion);
    }
    AndroidView(
        factory = { context: Context ->
            val clipView: HYTClip = HYTClip(context);
            clipView.setWillNotDraw(false);
            clipView.setBody(body);
            clipView.clipChildren = false;
            clipView.clipToOutline = false;
            view = clipView;
            clipView;
        },
        update = { clipView: HYTClip ->
            val root: ViewGroup? = clipView.parent as ViewGroup?;
            root?.setWillNotDraw(false);
            root?.clipToOutline = false;
            root?.clipChildren = false;
        },
        modifier = modifier
    )
}