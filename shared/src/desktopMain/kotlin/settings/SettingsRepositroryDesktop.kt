@file:Suppress("NewApi")

package settings

import com.charleskorn.kaml.Yaml
import com.github.javakeyring.Keyring
import com.github.javakeyring.PasswordAccessException
import dev.dirs.ProjectDirectories
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import player.PlayerDiscovery.getFirstAvailablePlayer
import player.PlayerDiscovery.isPlayerAvailable
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createFile
import kotlin.io.path.isReadable
import kotlin.io.path.notExists

actual fun getSettingsRepository(): SettingsRepository = SettingsRepositoryDesktop()

private const val SETTINGS_FILE = "potatotube.yml"
private const val KEYRING_SERVICE_NAME = "potatotube"

class SettingsRepositoryDesktop : SettingsRepository {
    private val keyring = Keyring.create()
    private val configDir = Path.of(
        ProjectDirectories.from("org", "snd", "PotatoTube").configDir
    )

    override suspend fun loadSettings(): Settings {
        return withContext(Dispatchers.IO) {
            val path = configDir.resolve(SETTINGS_FILE)
            val rawConfig = if (path.isReadable()) Files.readString(path) else null

            rawConfig?.let { Yaml.default.decodeFromString(Settings.serializer(), it) }
                ?.let {
                    if (it.player == null || !isPlayerAvailable(it.player.player)) it.copy(player = getFirstAvailablePlayer())
                    else it
                }
                ?: Settings(player = getFirstAvailablePlayer())
        }
    }

    override suspend fun saveSettings(settings: Settings) {
        withContext(Dispatchers.IO) {
            val path = configDir.resolve(SETTINGS_FILE)
            val configYml = Yaml.default.encodeToString(Settings.serializer(), settings)

            Files.createDirectories(path.parent)
            if (path.notExists()) path.createFile()
            Files.writeString(path, configYml)
        }
    }

    override fun loadPassword(user: String): String? {
        return try {
            keyring.getPassword(KEYRING_SERVICE_NAME, user.lowercase())
        } catch (ex: PasswordAccessException) {
            null
        }
    }

    override fun setPassword(user: String, password: String) {
        keyring.setPassword(KEYRING_SERVICE_NAME, user.lowercase(), password)
    }

    override fun deletePassword(user: String) {
        try {
            keyring.deletePassword(KEYRING_SERVICE_NAME, user.lowercase())
        } catch (e: Exception) {
            // ignore
        }
    }
}
