package org.hyt.hytport.visual.component.surface

import android.content.Context
import android.opengl.GLSurfaceView
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun surface(
    renderer: GLSurfaceView.Renderer,
    paused: Boolean = false,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context: Context ->
            val surface: GLSurfaceView = GLSurfaceView(context);
            surface.setEGLContextClientVersion(3);
            surface.setRenderer(
                renderer
            );
            surface;
        },
        update = {
            if (paused) {
                it.onPause();
            } else {
                it.onResume();
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .then(modifier)
    );
}