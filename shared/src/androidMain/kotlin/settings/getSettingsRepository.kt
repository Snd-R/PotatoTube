package settings

import player.PlayerType

actual fun getSettingsRepository(): SettingsRepository {
    return object : SettingsRepository {
        override suspend fun loadSettings(): Settings {
            return Settings(
                fontSize = 13f,
                emoteSize = 70f,
                isTimestampsEnabled = false,
                timestampFormat = "mm:ss",
                player = PlayerType("")
            )
        }

        override suspend fun saveSettings(settings: Settings) {
        }

        override fun loadPassword(user: String): String? {
            return null
        }

        override fun setPassword(user: String, password: String) {
        }

        override fun deletePassword(user: String) {
        }
    }
}