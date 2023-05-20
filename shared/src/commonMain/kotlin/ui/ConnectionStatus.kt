package ui

import kotlinx.coroutines.flow.MutableStateFlow

class ConnectionStatus {
    var currentUser = MutableStateFlow<String?>(null)
    var currentChannel = MutableStateFlow<String?>(null)
    var rank = MutableStateFlow(-1.0)
    var isGuest = MutableStateFlow(false)
    var kicked = MutableStateFlow(false)
    var disconnectReason = MutableStateFlow<String?>(null)


    fun connectedAndAuthenticated() = currentUser.value != null && currentChannel.value != null

    fun disconnect(disconnectReason: String? = null) {
        currentUser.value = null
        currentChannel.value = null
        this.disconnectReason.value = disconnectReason
    }

}
