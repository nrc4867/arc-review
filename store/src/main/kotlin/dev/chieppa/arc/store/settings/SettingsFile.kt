package dev.chieppa.arc.store.settings

import dev.chieppa.arc.store.files.AtomicFile
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.io.StringWriter
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
            lastReviewId = properties.getProperty(SettingsKeys.LAST_REVIEW)?.trim()?.ifEmpty { null },
            wrapLines = properties.getProperty(SettingsKeys.WRAP_LINES)?.trim()?.toBooleanStrictOrNull(),
            wholeLineHighlight = properties.getProperty(SettingsKeys.LINE_HIGHLIGHT)?.trim()?.toBooleanStrictOrNull(),
            startLayout = StartLayoutProperties.read(properties),
            mcpServer = properties.getProperty(SettingsKeys.MCP_SERVER)?.trim()?.toBooleanStrictOrNull(),
            mcpPort = properties.getProperty(SettingsKeys.MCP_PORT)?.trim()?.toIntOrNull()?.takeIf { it in 1..SettingsKeys.MAX_PORT },
        )
    }

    fun save(settings: AppSettings) {
        val properties = Properties()
        settings.themeId?.let { properties.setProperty(SettingsKeys.THEME, it) }
        settings.interfaceSize?.let { properties.setProperty(SettingsKeys.INTERFACE_SIZE, it.toString()) }
        settings.lastReviewId?.let { properties.setProperty(SettingsKeys.LAST_REVIEW, it) }
        settings.wrapLines?.let { properties.setProperty(SettingsKeys.WRAP_LINES, it.toString()) }
        settings.wholeLineHighlight?.let { properties.setProperty(SettingsKeys.LINE_HIGHLIGHT, it.toString()) }
        settings.mcpServer?.let { properties.setProperty(SettingsKeys.MCP_SERVER, it.toString()) }
        settings.mcpPort?.let { properties.setProperty(SettingsKeys.MCP_PORT, it.toString()) }
        StartLayoutProperties.write(settings.startLayout, properties)
        val text = StringWriter().also { properties.store(it, SettingsKeys.COMMENT) }.toString()
        AtomicFile.write(path, text)
    }
}
