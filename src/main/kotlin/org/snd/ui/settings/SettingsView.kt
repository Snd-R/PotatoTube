package org.snd.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.material.TextField
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.LoadState
import org.snd.ui.chat.Chat
import org.snd.ui.common.AppTheme
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Composable
fun SettingsView(model: SettingsModel, chat: Chat) {
    Row(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.30f)
                .fillMaxHeight()
                .background(color = AppTheme.colors.backgroundDarker)
        ) { SettingTabs(model, chat, Modifier.align(Alignment.TopEnd)) }

        Row(
            modifier = Modifier
                .widthIn(max = 600.dp)
        ) {
            Box(
                modifier = Modifier
                    .padding(30.dp)
            ) {
                when (model.currentTab) {
                    SettingsModel.CurrentTab.CHAT -> ChatSettings(model)
                    SettingsModel.CurrentTab.ACCOUNT -> AccountSettings(model)
                    SettingsModel.CurrentTab.CHANNEL -> ChannelSettings(model)
                    SettingsModel.CurrentTab.LOADING -> Loader()
                }
            }
        }
    }
}


@Composable
fun Loader() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxWidth().padding(20.dp)
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun SettingTabs(settings: SettingsModel, chat: Chat, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .widthIn(min = 300.dp, max = 300.dp)
            .padding(30.dp)
    ) {
        val coroutineScope = rememberCoroutineScope()
        Text(
            "General", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier
                .padding(bottom = 10.dp, start = 3.dp)
        )

        Text("Chat", modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 5.dp)
            .clickable(enabled = settings.isLoading.not()) { settings.currentTab = SettingsModel.CurrentTab.CHAT }
            .background(if (settings.currentTab == SettingsModel.CurrentTab.CHAT) AppTheme.colors.backgroundLight else AppTheme.colors.backgroundDarker)
        )

        Text("Account", modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 5.dp)
            .clickable(enabled = settings.isLoading.not()) {
                settings.currentTab = SettingsModel.CurrentTab.ACCOUNT
            }
            .background(if (settings.currentTab == SettingsModel.CurrentTab.ACCOUNT) AppTheme.colors.backgroundLight else AppTheme.colors.backgroundDarker)
        )
        Text("Channel", modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 5.dp)
            .clickable(enabled = settings.isLoading.not()) {
                settings.currentTab = SettingsModel.CurrentTab.CHANNEL
            }
            .background(if (settings.currentTab == SettingsModel.CurrentTab.CHANNEL) AppTheme.colors.backgroundLight else AppTheme.colors.backgroundDarker)
        )

        Divider()
        Row(modifier = Modifier
            .fillMaxWidth()
            .widthIn(50.dp)
            .padding(top = 10.dp)
            .clickable(enabled = settings.isLoading.not()) {
                coroutineScope.launch {
                    settings.currentTab = SettingsModel.CurrentTab.LOADING
                    settings.save()
                    chat.currentScreen = Chat.CurrentScreen.MAIN
                    settings.currentTab = SettingsModel.CurrentTab.CHAT
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
fun ChatSettings(settings: SettingsModel) {
    Column {
        Text("Chat settings")
        Spacer(Modifier.size(5.dp))
        Divider()
        Spacer(Modifier.size(15.dp))
        SliderOption(
            description = "Font Size",
            initialValue = settings.fontSize.value,
            valueRange = 6f..20f,
            steps = 13
        ) { settings.fontSize = it.roundToInt().sp }

        SliderOption(
            description = "Emote Size",
            initialValue = settings.emoteSize.value,
            valueRange = 50f..300f,
            steps = 0
        ) { settings.emoteSize = it.roundToInt().sp }
        var historySize by remember { mutableStateOf(settings.historySize.toString()) }
        TextField(
            historySize,
            onValueChange = { value ->
                if (value.length <= 5) {
                    historySize = value.filter { it.isDigit() }
                    if (historySize.isNotBlank())
                        settings.historySize = historySize.toInt()
                }
            },
            label = { Text("Message History size") },
        )
        Spacer(Modifier.size(15.dp))

        var timestampFormat by remember { mutableStateOf(settings.timestampFormat) }
        TextField(
            timestampFormat,
            onValueChange = { value ->
                try {
                    DateTimeFormatter.ofPattern(value)
                    timestampFormat = value
                    settings.timestampFormat = value
                } catch (e: IllegalArgumentException) {
                    //ignore
                }
            },
            label = { Text("Timestamp format") },
        )
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

@Composable
fun AccountSettings(model: SettingsModel) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row {
            Column {
                Text("Account Settings")
                val currentUser = if (model.username != null) "Current user: ${model.username}"
                else "Not user configured"
                Text(currentUser)
            }
        }

        Divider()
        if (model.username != null) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { model.logout() }) {
                    Text("Logout")
                }
            }
        } else {
            Login(model)
        }
    }
}

@Composable
fun Login(model: SettingsModel) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loginState by remember { mutableStateOf<LoadState<String>?>(null) }
    val coroutineScope = rememberCoroutineScope()

    when (val state = loginState) {
        null -> {}

        is LoadState.Loading -> {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                CircularProgressIndicator()
            }
        }

        is LoadState.Success -> {
            model.persistCredentials(state.value, password)
            username = ""
            password = ""
            model.isLoading = false
        }

        is LoadState.Error -> {
            Text("Authentication error: ${state.exception.message}")
            model.isLoading = false
        }
    }

    TextField(
        value = username,
        label = { Text(text = "User Name") },
        onValueChange = { username = it },
        enabled = model.isLoading.not()
    )
    TextField(
        value = password,
        label = { Text(text = "Password") },
        onValueChange = { password = it },
        visualTransformation = PasswordVisualTransformation(),
        enabled = model.isLoading.not()
    )

    Button(
        enabled = model.isLoading.not(),
        onClick = {
            model.isLoading = true
            loginState = LoadState.Loading()
            coroutineScope.launch {
                loginState = model.login(username, password)
            }
        }) {
        Text("Login")
    }
}

@Composable
fun ChannelSettings(settings: SettingsModel) {
    var text by remember { mutableStateOf("") }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Channel Settings")
        val currentChannelText = settings.channel?.let { "Current channel: ${settings.channel}" }
            ?: "Current channel is not set"
        Text(currentChannelText)

        if (settings.channel != null) {
            Button(onClick = {
                settings.disconnect()
            }) {
                Text("Disconnect")
            }
        }

        Divider()
        Row(modifier = Modifier.fillMaxWidth()) {
            TextField(
                value = text,
                label = { Text(text = "new channel") },
                onValueChange = { text = it },
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.size(10.dp))
            Button(
                onClick = {
                    if (text.isNotBlank())
                        settings.changeChannel(text)
                    text = ""
                }) {
                Text("Connect")
            }
        }
    }
}
