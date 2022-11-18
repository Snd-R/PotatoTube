package org.snd.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun AccountSettings(model: SettingsModel) {
    val coroutineScope = rememberCoroutineScope()
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        val currentUser = model.username ?: model.connectionStatus.currentUser
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
    val coroutineScope = rememberCoroutineScope()
    var loginError by remember { mutableStateOf<String?>(null) }

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

    if (loginError != null) {
        Text(
            text = "Authentication error: $loginError",
            style = TextStyle(color = MaterialTheme.colors.error, fontWeight = FontWeight.Bold),
        )
    }

    Button(
        onClick = {

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
        }) {
        Text("Login")
    }

}
