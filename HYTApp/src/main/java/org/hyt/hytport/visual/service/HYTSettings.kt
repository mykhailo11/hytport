package org.hyt.hytport.visual.service

import android.content.SharedPreferences
import android.opengl.GLSurfaceView
import android.os.Bundle
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hyt.hytport.R
import org.hyt.hytport.audio.api.model.HYTAudioModel
import org.hyt.hytport.audio.api.service.HYTBinder
import org.hyt.hytport.graphics.factory.HYTGLFactory
import org.hyt.hytport.util.HYTMathUtil
import org.hyt.hytport.util.HYTUtil
import org.hyt.hytport.visual.api.model.HYTSettingState
import org.hyt.hytport.visual.api.model.HYTState
import org.hyt.hytport.visual.component.loading.loadingIcon
import org.hyt.hytport.visual.component.montserrat
import org.hyt.hytport.visual.component.settings.rememberSettingState
import org.hyt.hytport.visual.component.settings.setting
import org.hyt.hytport.visual.component.surface.surface
import org.hyt.hytport.visual.factory.HYTStateFactory
import java.util.concurrent.ScheduledExecutorService

class HYTSettings : HYTBaseActivity() {

    companion object {

        private val _STATES: Int = 6;

        private data class HYTSetting(
            val key: String,
            val title: String,
            val description: String,
            var state: Boolean
        );

    }

    private lateinit var _canvas: GLSurfaceView.Renderer;

    private lateinit var _consumer: (ByteArray) -> Unit;

    private val _settings: MutableList<HYTSetting> = mutableListOf();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        val speed: Float = 60.0f / _rate;
        val balanceStates: Array<HYTState> = Array(_STATES) {
            HYTStateFactory.getBalanceState(0.02f * speed);
        };
        _consumer = { food: ByteArray ->
            val result: Float = HYTMathUtil.getNormalBytesAverage(food);
            val chosen: Int = (Math.random() * _STATES).toInt();
            balanceStates[chosen].setState(result);
        }
        _canvas = HYTGLFactory.getCanvas(
            this,
            HYTUtil.readSource(resources.getString(R.string.settings_vertex_shader), assets),
            HYTUtil.readSource(resources.getString(R.string.settings_shader), assets),
            mapOf(
                Pair(resources.getString(R.string.balance_states), balanceStates)
            )
        );
        _settings.add(
            HYTSetting(
                key = resources.getString(R.string.settings_wake_lock),
                title = "Wake Lock",
                description = "keep screen on while on the player view",
                state = _preferences.getBoolean(
                    resources.getString(R.string.settings_wake_lock),
                    true
                )
            )
        );
        _settings.add(
            HYTSetting(
                key = resources.getString(R.string.setting_show_expanded),
                title = "Expand",
                description = "always show expanded controls",
                state = _preferences.getBoolean(
                    resources.getString(R.string.setting_show_expanded),
                    false
                )
            )
        );
        _settings.add(
            HYTSetting(
                key = resources.getString(R.string.settings_audio_focus),
                title = "Respect Audio Focus",
                description = "playback can be interrupted by other apps",
                state = _preferences.getBoolean(
                    resources.getString(R.string.settings_audio_focus),
                    true
                )
            )
        );
    }

    @Composable
    override fun compose(player: HYTBinder, executor: ScheduledExecutorService) {
        DisposableEffect(player) {
            val auditor: HYTBinder.Companion.HYTAuditor =
                object : HYTBinder.Companion.HYTAuditor {

                    override fun consumer(food: ByteArray) {
                        _consumer(food);
                    }

                    override fun onReady(audio: HYTAudioModel?, current: Long) {
                        super.onReady(audio, current)
                    }

                };
            player.addAuditor(auditor);
            onDispose {
                player.removeAuditor(auditor);
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            surface(
                renderer = _canvas,
                paused = false
            );
            Column(
                verticalArrangement = Arrangement.spacedBy(15.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = colorResource(R.color.hyt_semi_dark)
                        )
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
                                finish();
                            }
                    );
                    Text(
                        textAlign = TextAlign.Center,
                        fontFamily = montserrat,
                        text = remember { "Settings" },
                        color = colorResource(R.color.hyt_text_dark),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .weight(1.0f)
                            .padding(
                                end = 20.dp
                            )
                    );
                }
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(15.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 10.dp
                        )
                ) {
                    items(
                        items = _settings
                    ) { item: HYTSetting ->
                        val settingState: HYTSettingState = rememberSettingState(
                            initial = item.state,
                            initialTitle = item.title,
                            initialDescription = item.description
                        );
                        DisposableEffect(settingState) {
                            val auditor: HYTSettingState.Companion.HYTAuditor = object
                                : HYTSettingState.Companion.HYTAuditor {

                                override fun onToggle(toggle: Boolean) {
                                    item.state = toggle;
                                }

                            };
                            settingState.addAuditor(auditor);
                            onDispose {
                                settingState.removeAuditor(auditor);
                            }
                        }
                        setting(
                            state = settingState,
                            modifier = Modifier
                                .padding(20.dp)
                        );
                    }
                }
            }
        }
    }

    @Composable
    override fun loading() {
        loadingIcon();
    }

    override fun onDestroy() {
        val editor: SharedPreferences.Editor = _preferences.edit();
        _settings.forEach { setting: HYTSetting ->
            editor
                .putBoolean(setting.key, setting.state)
        }
        editor.apply();
        super.onDestroy()
    }

}