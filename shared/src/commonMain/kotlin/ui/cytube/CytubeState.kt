package ui.cytube

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class CytubeState {
    var authenticated by mutableStateOf(false)
    var currentChannel by mutableStateOf<String?>(null)
    var username by mutableStateOf("")
}
