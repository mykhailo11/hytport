package org.hyt.hytport.visual.service

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hyt.hytport.R

class HYTInit : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var playbackGranted: Boolean by remember { mutableStateOf(false) };
            var processingGranted: Boolean by remember { mutableStateOf(false) };
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
            if (!playbackGranted) {
                _permission(
                    title = "PLAYBACK PERMISSION",
                    content = buildAnnotatedString { append(stringResource(R.string.hyt_playback_permission)) },
                ) {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.MODIFY_AUDIO_SETTINGS,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.FOREGROUND_SERVICE,
                            Manifest.permission.INTERNET,
                            Manifest.permission.WAKE_LOCK
                        )
                    );
                };
            } else if (!processingGranted) {
                val captureContent: AnnotatedString = buildAnnotatedString {
                    val normalPart: String = stringResource(R.string.hyt_capture_permission) + " ";
                    val origin: String = stringResource(R.string.hyt_capture_permission_note);
                    append(normalPart);
                    append(origin);
                    addStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.ExtraBold,
                            color = colorResource(R.color.hyt_white)
                        ),
                        start = normalPart.length,
                        end = origin.length + normalPart.length
                    );
                }
                _permission(
                    title = "CAPTURE PERMISSION",
                    content = captureContent
                ) {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.RECORD_AUDIO
                        )
                    );
                };
            } else if (!policyAccepted) {
                val origin: String = stringResource(R.string.hyt_privacy_policy_accept);
                val privacyContent: AnnotatedString = buildAnnotatedString {
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
                        annotation = "https://www.freeprivacypolicy.com/live/209ea70c-66ad-4e04-9757-1e5188504bdf",
                        start = start,
                        end = end
                    );
                }
                _permission(
                    title = "PRIVACY POLICY",
                    content = privacyContent,
                    contentClick = {
                        privacyContent.getStringAnnotations("URL", 0, origin.length)
                            .firstOrNull()?.let { range: AnnotatedString.Range<String> ->
                                uriHandler.openUri(range.item);
                            }
                    }
                ) {
                    policyAccepted = true;
                }
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
                startActivityIfNeeded(Intent(this, HYTApp::class.java), 100);
                finish();
            }
        }
    }

    @Composable
    private fun _permission(
        title: String,
        content: AnnotatedString,
        contentClick: (() -> Unit)? = null,
        accept: () -> Unit
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        Pair(0.0f, colorResource(R.color.hyt_accent_dark)),
                        Pair(0.2f, colorResource(R.color.hyt_dark))
                    )
                )
                .padding(
                    horizontal = 50.dp,
                    vertical = 100.dp
                )
        ) {
            Text(
                textAlign = TextAlign.Center,
                text = title,
                color = colorResource(R.color.hyt_accent),
                fontSize = 35.sp
            );
            Text(
                textAlign = TextAlign.Center,
                text = content,
                color = colorResource(R.color.hyt_text_dark),
                lineHeight = 35.sp,
                fontSize = 18.sp,
                modifier = Modifier
                    .clickable {
                        if (contentClick != null) {
                            contentClick();
                        }
                    }
            );
            Button(
                onClick = {
                    accept()
                },
                contentPadding = PaddingValues(
                    horizontal = 40.dp,
                    vertical = 20.dp
                ),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = colorResource(R.color.hyt_accent)
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(15))
            ) {
                Text(
                    text = "Accept",
                    fontSize = 18.sp,
                    color = colorResource(R.color.hyt_black)
                );
            }
        }
    }

}