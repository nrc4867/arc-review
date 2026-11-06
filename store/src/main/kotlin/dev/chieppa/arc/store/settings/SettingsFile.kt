package dev.chieppa.arc.store.settings

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.Properties

/**
 * Keeps [AppSettings] in a properties file at [path]. A missing or unreadable file, or a value that does not parse,
 * reads as not chosen. Saving writes a new file and moves it into place, so a crash mid-save leaves the old one.
 */
class SettingsFile(private val path: Path) {
    fun load(): AppSettings {
        val properties = Properties()
        try {
            Files.newBufferedReader(path).use(properties::load)
        } catch (_: IOException) {
            return AppSettings()
        } catch (_: IllegalArgumentException) {
            return AppSettings()
        }
        return AppSettings(
            themeId = properties.getProperty(SettingsKeys.THEME)?.trim()?.ifEmpty { null },
            interfaceSize = properties.getProperty(SettingsKeys.INTERFACE_SIZE)?.trim()?.toFloatOrNull()?.takeIf { it.isFinite() && it > 0f },
        )
    }

    fun save(settings: AppSettings) {
        val properties = Properties()
        settings.themeId?.let { properties.setProperty(SettingsKeys.THEME, it) }
        settings.interfaceSize?.let { properties.setProperty(SettingsKeys.INTERFACE_SIZE, it.toString()) }
        path.toAbsolutePath().parent?.let(Files::createDirectories)
        val temp = path.resolveSibling(path.fileName.toString() + SettingsKeys.TEMP_SUFFIX)
        Files.newBufferedWriter(temp).use { properties.store(it, SettingsKeys.COMMENT) }
        Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
    }
}
