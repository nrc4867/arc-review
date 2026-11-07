package dev.chieppa.arc.store.settings

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createTempDirectory
import kotlin.io.path.exists
import kotlin.io.path.writeText
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class SettingsFileTest {
    private val folder: Path = createTempDirectory("arc-settings")
    private val path = folder.resolve("nested/settings.properties")

    @AfterTest
    fun cleanUp() {
        folder.toFile().deleteRecursively()
    }

    @Test
    fun aMissingFileHasNothingChosen() {
        assertEquals(AppSettings(), SettingsFile(path).load())
    }

    @Test
    fun savedSettingsLoadBack() {
        val settings = AppSettings(themeId = "workshop-light", interfaceSize = 1.25f, lastReviewId = "r-1", wrapLines = true, gutterHighlight = true)
        SettingsFile(path).save(settings)
        assertEquals(settings, SettingsFile(path).load())
        assertFalse(path.resolveSibling("settings.properties.tmp").exists())
    }

    @Test
    fun savingAgainReplacesTheFile() {
        SettingsFile(path).save(AppSettings(themeId = "ink", interfaceSize = 1.5f))
        SettingsFile(path).save(AppSettings(themeId = "ink-dark"))
        assertEquals(AppSettings(themeId = "ink-dark"), SettingsFile(path).load())
    }

    @Test
    fun valuesThatDoNotParseAreNotChosen() {
        Files.createDirectories(path.parent)
        path.writeText("theme=\ninterface.size=big\n")
        assertEquals(AppSettings(), SettingsFile(path).load())
        path.writeText("interface.size=-1\n")
        assertEquals(AppSettings(), SettingsFile(path).load())
    }
}
