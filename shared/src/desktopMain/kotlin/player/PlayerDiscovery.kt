package player

import player.Player.*
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery

object PlayerDiscovery {
    private val OS_NAME = System.getProperty("os.name").lowercase()
    private val isMac = OS_NAME.contains("mac")

    var isMpvEmbeddedAvailable: Boolean = false
        private set
    var isVlcAvailable: Boolean = false
        private set
    var isVlcEmbeddedAvailable: Boolean = false
        private set


    fun discover() {
        if (NativeDiscovery().discover()) {
            isVlcAvailable = true
            if (!isMac) isVlcEmbeddedAvailable = true
        }

        if (!isMac) runCatching {
            System.loadLibrary("mpv")
            isMpvEmbeddedAvailable = true
        }
    }

    fun isPlayerAvailable(player: Player) = when (player) {
        VLC_EMBEDDED -> isVlcEmbeddedAvailable
        VLC_DIRECT -> isVlcAvailable
        MPV_EMBEDDED -> isMpvEmbeddedAvailable
    }

    fun getFirstAvailablePlayer() = when {
        isMpvEmbeddedAvailable -> PlayerType(MPV_EMBEDDED)
        isVlcEmbeddedAvailable -> PlayerType(VLC_EMBEDDED)
        isVlcAvailable -> PlayerType(VLC_DIRECT)
        else -> null
    }
}
