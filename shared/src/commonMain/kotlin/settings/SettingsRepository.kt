package settings

expect fun getSettingsRepository(): SettingsRepository


interface SettingsRepository {

    suspend fun loadSettings(): Settings

    suspend fun saveSettings(settings: Settings)

    fun loadPassword(user: String): String?

    fun setPassword(user: String, password: String)

    fun deletePassword(user: String)

}
