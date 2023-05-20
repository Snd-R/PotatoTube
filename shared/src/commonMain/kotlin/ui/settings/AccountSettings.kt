package ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun AccountSettings(model: SettingsState) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        val currentUser = model.username ?: model.connectionStatus.currentUser.collectAsState().value
        Row {
            Column {
                Text("Account Settings")
                val currentUserText = currentUser?.let { "Current user: $it" }
                    ?: "Not logged in"

                Text(currentUserText)
            }
        }

        Divider()
        if (currentUser != null) {
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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Login(model: SettingsState) {
    val focusManager = LocalFocusManager.current
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    var loginError by remember { mutableStateOf<String?>(null) }

    fun login() {
        model.isLoading = true
        coroutineScope.launch {
            try {
                model.login(username, password)
                model.persistCredentials(username, password)

            } catch (e: Exception) {
                loginError = e.message
            }
            model.isLoading = false
        }
    }

    val textFieldModifier = Modifier.onPreviewKeyEvent {
        when {
            it.key == Key.Enter && it.type == KeyEventType.KeyDown -> {
                if (password == "" && !model.allowGuestLogin)
                    loginError = "Guest login is not allowed on this screen"
                else login()
                true
            }

            it.key == Key.Tab && it.type == KeyEventType.KeyDown -> {
                focusManager.moveFocus(FocusDirection.Down)
                true
            }

            else -> false
        }
    }

    TextField(
        value = username,
        label = { Text(text = "User Name") },
        onValueChange = { username = it.trim() },
        modifier = textFieldModifier
    )
    TextField(
        value = password,
        label = { Text(text = "Password") },
        onValueChange = { password = it.trim() },
        visualTransformation = PasswordVisualTransformation(),
        modifier = textFieldModifier,
    )

    if (loginError != null) {
        Text(
            text = "Authentication error: $loginError",
            style = TextStyle(color = MaterialTheme.colors.error, fontWeight = FontWeight.Bold),
        )
    }

    Button(
        onClick = {
            login()
        }) {
        Text("Login")
    }
}
