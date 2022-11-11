package org.snd.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.snd.ui.common.AppTheme
import org.snd.ui.util.LoaderOverlay
import kotlin.math.roundToInt

@Composable
fun SettingsView(model: SettingsModel) {
    Row(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.30f)
                .fillMaxHeight()
                .background(color = AppTheme.colors.backgroundDarker)
        ) { SettingTabs(model, Modifier.align(Alignment.TopEnd)) }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(color = AppTheme.colors.backgroundMedium)
        ) {
            Box(
                modifier = Modifier
                    .padding(10.dp)
            ) {
                when (model.currentTab) {
                    SettingsModel.CurrentTab.CHAT -> ChatSettings(model)
                    SettingsModel.CurrentTab.ACCOUNT -> AccountSettings(model)
                    SettingsModel.CurrentTab.CHANNEL -> ChannelSettings(model)
                    SettingsModel.CurrentTab.PLAYBACK -> PlaybackSettings(model)
                }
            }
        }
    }
    if (model.isLoading) {
        LoaderOverlay()
    }
}

@Composable
fun SettingTabs(settings: SettingsModel, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .widthIn(min = 300.dp, max = 300.dp)
            .padding(10.dp)
    ) {
        val coroutineScope = rememberCoroutineScope()
        Text(
            "General", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier
                .padding(bottom = 10.dp, start = 3.dp)
        )

        Text("Chat", modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 5.dp)
            .clickable { settings.currentTab = SettingsModel.CurrentTab.CHAT }
            .background(if (settings.currentTab == SettingsModel.CurrentTab.CHAT) AppTheme.colors.backgroundLight else AppTheme.colors.backgroundDarker)
        )

        Text("Account", modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 5.dp)
            .clickable {
                settings.currentTab = SettingsModel.CurrentTab.ACCOUNT
            }
            .background(if (settings.currentTab == SettingsModel.CurrentTab.ACCOUNT) AppTheme.colors.backgroundLight else AppTheme.colors.backgroundDarker)
        )
        Text("Channel", modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 5.dp)
            .clickable {
                settings.currentTab = SettingsModel.CurrentTab.CHANNEL
            }
            .background(if (settings.currentTab == SettingsModel.CurrentTab.CHANNEL) AppTheme.colors.backgroundLight else AppTheme.colors.backgroundDarker)
        )
        Text("Playback", modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 5.dp)
            .clickable {
                settings.currentTab = SettingsModel.CurrentTab.PLAYBACK
            }
            .background(if (settings.currentTab == SettingsModel.CurrentTab.PLAYBACK) AppTheme.colors.backgroundLight else AppTheme.colors.backgroundDarker)
        )

        Divider()
        Row(modifier = Modifier
            .fillMaxWidth()
            .widthIn(50.dp)
            .padding(top = 10.dp)
            .clickable {
                coroutineScope.launch {
                    settings.isLoading = true
                    settings.save()
                    settings.currentTab = SettingsModel.CurrentTab.CHAT
                    settings.isLoading = false
                    settings.isActiveScreen = false
                }
            }
        ) {
            Text("Return", modifier = Modifier.weight(1f))
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Return",
                tint = Color.LightGray,
                modifier = modifier
                    .width(22.dp)
                    .height(22.dp)
            )
        }

    }
}


@Composable
fun SliderOption(
    description: String,
    initialValue: Float,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    onChange: (value: Float) -> Unit = {}
) {
    var sliderPosition by remember { mutableStateOf(initialValue) }
    Text(description)
    Row(modifier = Modifier.fillMaxWidth()) {
        Slider(
            value = sliderPosition,
            onValueChange = {
                sliderPosition = it
                onChange(it)
            },
            steps = steps,
            valueRange = valueRange,
            modifier = Modifier.fillMaxWidth(0.9f)
        )
        Text(sliderPosition.roundToInt().toString(), modifier = Modifier.align(Alignment.CenterVertically))
    }
}


