package org.hyt.hytport.visual.service

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.opengl.GLSurfaceView
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import org.hyt.hytport.R
import org.hyt.hytport.graphics.factory.HYTGLFactory
import org.hyt.hytport.util.HYTUtil
import org.hyt.hytport.visual.component.surface.pop
import org.hyt.hytport.visual.component.surface.surface

class HYTInit : ComponentActivity() {

    private lateinit var _canvas: GLSurfaceView.Renderer;

    private var _paused: ((Boolean) -> Unit)? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _canvas = HYTGLFactory.getCanvas(
            this,
            HYTUtil.readSource(resources.getString(R.string.init_vertex_shader), assets),
            HYTUtil.readSource(resources.getString(R.string.init_shader), assets),
            emptyMap()
        )
        setContent {
            var playbackGranted: Boolean by remember {
                mutableStateOf(
                    checkSelfPermission(
                        Manifest.permission.MODIFY_AUDIO_SETTINGS
                    ) == PackageManager.PERMISSION_GRANTED
                            && checkSelfPermission(
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                            && (Build.VERSION.SDK_INT < Build.VERSION_CODES.P || checkSelfPermission(
                        Manifest.permission.FOREGROUND_SERVICE
                    ) == PackageManager.PERMISSION_GRANTED)
                            && checkSelfPermission(
                        Manifest.permission.INTERNET
                    ) == PackageManager.PERMISSION_GRANTED
                            && checkSelfPermission(
                        Manifest.permission.WAKE_LOCK
                    ) == PackageManager.PERMISSION_GRANTED
                )
            };
            var processingGranted: Boolean by remember {
                mutableStateOf(
                    checkSelfPermission(
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                )
            };
            var policyAccepted: Boolean by remember { mutableStateOf(false) };
            val uriHandler: UriHandler = LocalUriHandler.current;
            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions: Map<String, Boolean> ->
                val modifyAudioSettings: Boolean? = permissions[Manifest.permission.MODIFY_AUDIO_SETTINGS];
                val readExternalStorage: Boolean? = permissions[Manifest.permission.READ_EXTERNAL_STORAGE];
                val foregroundService: Boolean? =
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P)
                        permissions[Manifest.permission.FOREGROUND_SERVICE]
                    else true;
                val internet: Boolean? = permissions[Manifest.permission.INTERNET];
                val wakeLock: Boolean? = permissions[Manifest.permission.WAKE_LOCK];
                val recordAudio: Boolean? = permissions[Manifest.permission.RECORD_AUDIO];
                playbackGranted = playbackGranted
                        || (modifyAudioSettings != null && modifyAudioSettings
                        && readExternalStorage != null && readExternalStorage
                        && foregroundService != null && foregroundService
                        && internet != null && internet
                        && wakeLock != null && wakeLock);
                processingGranted = processingGranted
                        || recordAudio != null && recordAudio;
            }
            val title: String by derivedStateOf {
                if (!playbackGranted) {
                    "PLAYBACK PERMISSION"
                } else if (!processingGranted) {
                    "CAPTURE PERMISSION"
                } else if (!policyAccepted) {
                    "PRIVACY POLICY"
                } else {
                    ""
                }
            };
            val content: AnnotatedString = buildAnnotatedString {
                if (!playbackGranted) {
                    append(stringResource(R.string.hyt_playback_permission))
                } else if (!processingGranted) {
                    val normalPart: String = stringResource(R.string.hyt_capture_permission);
                    val origin: String = stringResource(R.string.hyt_capture_permission_note);
                    append(normalPart);
                    append(" ")
                    append(origin);
                    addStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.ExtraBold,
                            color = colorResource(R.color.hyt_white)
                        ),
                        start = normalPart.length,
                        end = origin.length + normalPart.length
                    );
                } else if (!policyAccepted) {
                    val origin: String = stringResource(R.string.hyt_privacy_policy_accept);
                    val part: String = "Privacy Policy";
                    val start: Int = origin.indexOf(part);
                    val end: Int = start + part.length
                    append(origin);
                    addStyle(
                        style = SpanStyle(
                            color = colorResource(R.color.hyt_grey),
                            textDecoration = TextDecoration.Underline,
                        ),
                        start = start,
                        end = end
                    );
                    addStringAnnotation(
                        tag = "URL",
                        annotation = stringResource(R.string.hyt_privacy_link),
                        start = start,
                        end = end
                    );
                } else {
                    append("")
                }
            }
            val launcher: () -> Unit by derivedStateOf {
                if (!playbackGranted) {
                    {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.FOREGROUND_SERVICE,
                                Manifest.permission.INTERNET,
                                Manifest.permission.WAKE_LOCK
                            )
                        );
                    }
                } else if (!processingGranted) {
                    {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.RECORD_AUDIO
                            )
                        );
                    }
                } else if (!policyAccepted) {
                    {
                        policyAccepted = true;
                    }
                } else {
                    {}
                }
            }
            val origin: String = stringResource(R.string.hyt_privacy_policy_accept);
            val contentClick: () -> Unit by derivedStateOf {
                if (!policyAccepted) {
                    {
                        content.getStringAnnotations("URL", 0, origin.length)
                            .firstOrNull()?.let { range: AnnotatedString.Range<String> ->
                                uriHandler.openUri(range.item);
                            }
                    }
                } else {
                    {}
                }
            };

            if (playbackGranted && processingGranted && policyAccepted) {
                getSharedPreferences(
                    resources.getString(R.string.preferences),
                    Context.MODE_PRIVATE
                )
                    .edit()
                    .putBoolean(
                        resources.getString(R.string.preferences_permissions),
                        true
                    )
                    .commit();
                startActivityIfNeeded(Intent(this, HYTApp::class.java), 100);
                finish();
            } else {
                _permission(
                    title = title,
                    content = content,
                    contentClick = contentClick
                ) {
                    launcher();
                }
            }
        }
    }

    @Composable
    private fun _permission(
        title: String,
        content: AnnotatedString,
        confirm: String = "Accept",
        contentClick: (() -> Unit)? = null,
        modifier: Modifier = Modifier,
        accept: () -> Unit
    ) {
        val context: Context = LocalContext.current;
        val paused: Boolean by produceState(
            initialValue = false,
            context
        ) {
            _paused = { pause: Boolean ->
                value = pause;
            }
        };
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            surface(
                renderer = _canvas,
                paused = paused
            );
            pop(
                title = title,
                content = content,
                contentClick = contentClick,
                confirm = confirm,
                accept = accept
            );
        }
    }

    override fun onPause() {
        if (_paused != null) {
            _paused!!(true);
        }
        super.onPause()
    }

    override fun onResume() {
        if (_paused != null) {
            _paused!!(false);
        }
        super.onResume()
    }

}