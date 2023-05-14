package ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import ui.common.AppTheme
import kotlin.math.roundToInt


@Composable
fun SettingsView(model: SettingsState, onDismiss: () -> Unit) {
    val focusManager = LocalFocusManager.current

    Box(Modifier.pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.30f)
                    .fillMaxHeight()
                    .background(color = AppTheme.colors.backgroundDarker)
            ) { SettingTabs(model, Modifier.align(Alignment.TopEnd), onDismiss) }

            Row(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxSize()
                    .background(color = AppTheme.colors.backgroundMedium)
            ) {
                Box(
                    modifier = Modifier
                        .padding(10.dp)
                ) {
                    when (model.currentTab) {
                        SettingsState.CurrentTab.CHAT -> ChatSettings(model)
                        SettingsState.CurrentTab.ACCOUNT -> AccountSettings(model)
                        SettingsState.CurrentTab.CHANNEL -> ChannelSettings(model)
                        SettingsState.CurrentTab.PLAYBACK -> PlaybackSettings(model)
                    }
                }
            }
            Box(modifier = Modifier.fillMaxWidth())
        }
        if (model.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .pointerInput(Unit) {},
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun SettingTabs(settings: SettingsState, modifier: Modifier = Modifier, onDismiss: () -> Unit) {
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
            .clickable { settings.currentTab = SettingsState.CurrentTab.CHAT }
            .background(
                if (settings.currentTab == SettingsState.CurrentTab.CHAT) AppTheme.colors.backgroundLight
                else AppTheme.colors.backgroundDarker
            )
        )

        Text("Account", modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 5.dp)
            .clickable { settings.currentTab = SettingsState.CurrentTab.ACCOUNT }
            .background(
                if (settings.currentTab == SettingsState.CurrentTab.ACCOUNT) AppTheme.colors.backgroundLight
                else AppTheme.colors.backgroundDarker
            )
        )
        Text("Channel", modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 5.dp)
            .clickable { settings.currentTab = SettingsState.CurrentTab.CHANNEL }
            .background(
                if (settings.currentTab == SettingsState.CurrentTab.CHANNEL) AppTheme.colors.backgroundLight
                else AppTheme.colors.backgroundDarker
            )
        )
        Text("Playback", modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 5.dp)
            .clickable { settings.currentTab = SettingsState.CurrentTab.PLAYBACK }
            .background(
                if (settings.currentTab == SettingsState.CurrentTab.PLAYBACK) AppTheme.colors.backgroundLight
                else AppTheme.colors.backgroundDarker
            )
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
                    settings.currentTab = SettingsState.CurrentTab.CHAT
                    settings.isLoading = false
                    settings.isActiveScreen = false
                    onDismiss()
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


