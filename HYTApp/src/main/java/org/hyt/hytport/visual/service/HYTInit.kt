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
import org.hyt.hytport.visual.api.model.HYTPopState
import org.hyt.hytport.visual.component.surface.pop
import org.hyt.hytport.visual.component.surface.rememberPopState
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
            val context: Context = LocalContext.current;
            val uriHandler: UriHandler = LocalUriHandler.current;
            val playbackContent: AnnotatedString = buildAnnotatedString {
                append(stringResource(R.string.hyt_playback_permission))
            }
            val processingContent: AnnotatedString = buildAnnotatedString {
                append(stringResource(R.string.hyt_capture_permission));
                append(" ")
                pushStyle(
                    SpanStyle(
                        color = colorResource(R.color.hyt_white),
                        fontWeight = FontWeight.ExtraBold
                    )
                )
                append(stringResource(R.string.hyt_capture_permission_note));
                pop();
            }
            val origin: String = stringResource(R.string.hyt_privacy_policy_accept);
            val privacy: String = stringResource(R.string.hyt_privacy_policy_link);
            val privacyContent: AnnotatedString = buildAnnotatedString {
                append(origin);
                append(" ");
                pushStyle(
                    SpanStyle(
                        color = colorResource(R.color.hyt_grey),
                        textDecoration = TextDecoration.Underline,
                    )
                );
                pushStringAnnotation(
                    tag = "URL",
                    annotation = stringResource(R.string.hyt_privacy_link)
                )
                append(privacy);
                pop();
            }


            val popState: HYTPopState = rememberPopState(
                initialContent = playbackContent,
                initialConfirm = remember {
                    "Accept"
                },
                initialTitle = remember {
                    "Playback Permission"
                }
            );
            var privacyAccepted: Boolean by remember { mutableStateOf(false) };
            var playbackGranted: Boolean by remember(popState) {
                mutableStateOf(
                    (checkSelfPermission(
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
                    ) == PackageManager.PERMISSION_GRANTED)
                )
            };
            var processingGranted: Boolean by remember {
                mutableStateOf(
                    (checkSelfPermission(
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED)
                )
            }
            LaunchedEffect(
                privacyAccepted,
                playbackGranted,
                processingGranted
            ) {
                if (!playbackGranted) {
                    popState.content(playbackContent);
                    popState.title("Playback Permission");
                } else if (!processingGranted) {
                    popState.content(processingContent);
                    popState.title("Capture Permission");
                } else if (!privacyAccepted) {
                    popState.content(privacyContent);
                    popState.title("Privacy Policy");
                } else {
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
                    startActivityIfNeeded(Intent(context, HYTApp::class.java), 100);
                    finish();
                }
            }
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
                playbackGranted = playbackGranted ||
                        (modifyAudioSettings != null && modifyAudioSettings
                        && readExternalStorage != null && readExternalStorage
                        && foregroundService != null && foregroundService
                        && internet != null && internet
                        && wakeLock != null && wakeLock);
                processingGranted = (processingGranted || recordAudio != null && recordAudio);
            }
            DisposableEffect(popState) {
                val auditor: HYTPopState.Companion.HYTAuditor = object :
                    HYTPopState.Companion.HYTAuditor {

                    override fun onAccept() {
                        if (!playbackGranted) {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.MODIFY_AUDIO_SETTINGS,
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.FOREGROUND_SERVICE,
                                    Manifest.permission.INTERNET,
                                    Manifest.permission.WAKE_LOCK
                                )
                            )
                        } else if (!processingGranted) {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.RECORD_AUDIO
                                )
                            );
                        } else if (!privacyAccepted) {
                            privacyAccepted = true;
                        }
                    }

                    override fun onClick() {
                        if (!privacyAccepted) {
                            processingContent.getStringAnnotations(
                                "URL",
                                0,
                                origin.length + privacy.length
                            ).firstOrNull()?.let { range: AnnotatedString.Range<String> ->
                                uriHandler.openUri(range.item);
                            }
                        }
                    }

                }
                popState.addAuditor(auditor);
                onDispose {
                    popState.removeAuditor(auditor);
                }
            }
            _permission(
                popState = popState
            )
        }
    }

    @Composable
    private fun _permission(
        popState: HYTPopState,
        modifier: Modifier = Modifier,
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
                state = popState
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