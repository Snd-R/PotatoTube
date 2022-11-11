package org.snd.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.LoadState

@Composable
fun AccountSettings(model: SettingsModel) {
    val coroutineScope = rememberCoroutineScope()
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
                Button(onClick = {
                    model.isLoading = true
                    coroutineScope.launch {
                        model.logout()
                        model.isLoading = false
                    }
                }) {
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
            model.isLoading = true
        }

        is LoadState.Success -> {
            model.persistCredentials(state.value, password)
            username = ""
            password = ""
            model.isLoading = false
            loginState = null
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
    )
    TextField(
        value = password,
        label = { Text(text = "Password") },
        onValueChange = { password = it },
        visualTransformation = PasswordVisualTransformation(),
    )

    Button(
        onClick = {
            loginState = LoadState.Loading()
            coroutineScope.launch {
                try {
                    username = model.login(username, password)
                    loginState = LoadState.Success(username)
                } catch (e: Exception) {
                    loginState = LoadState.Error(e)
                }
            }
        }) {
        Text("Login")
    }
}
