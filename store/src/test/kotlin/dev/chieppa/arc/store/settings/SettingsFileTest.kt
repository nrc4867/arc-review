package dev.chieppa.arc.store.settings

import dev.chieppa.arc.core.start.StartColumn
import dev.chieppa.arc.core.start.StartLayout
import dev.chieppa.arc.core.start.StartMove
import dev.chieppa.arc.core.start.StartScreenKind
import dev.chieppa.arc.core.start.StartScreenLayout
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
        val settings = AppSettings(themeId = "workshop-light", interfaceSize = 1.25f, lastReviewId = "r-1", wrapLines = true, wholeLineHighlight = false, mcpServer = true, mcpPort = 47_900)
        SettingsFile(path).save(settings)
        assertEquals(settings, SettingsFile(path).load())
        assertFalse(path.resolveSibling("settings.properties.tmp").exists())
    }

    @Test
    fun theStartScreensLayoutLoadsBackPerScreen() {
        val layout = StartLayout()
            .with(StartScreenKind.NothingOpen, StartScreenLayout(hidden = setOf("arc.keys", "arc.help")))
            .with(StartScreenKind.OneSource, StartScreenLayout(shown = setOf("arc.notes"), moves = mapOf("arc.notes" to StartMove(StartColumn.Side, 2))))
        SettingsFile(path).save(AppSettings(startLayout = layout))
        assertEquals(layout, SettingsFile(path).load().startLayout)
    }

    @Test
    fun startMovesThatDoNotParseAreLeftOut() {
        Files.createDirectories(path.parent)
        path.writeText("start.one-source.moves=arc.a:side:1,arc.b:middle:0,arc.c:main:-1,arc.d\n")
        assertEquals(mapOf("arc.a" to StartMove(StartColumn.Side, 1)), SettingsFile(path).load().startLayout.of(StartScreenKind.OneSource).moves)
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
