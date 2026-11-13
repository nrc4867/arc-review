package dev.chieppa.arc.install

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createTempDirectory
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LauncherFilesTest {
    private val dir = createTempDirectory("arc-launchers")
    private val layout = InstallLayout(dir.resolve("data"))
    private val places = LauncherPlaces(dir.resolve("start"), dir.resolve("pins"), dir.resolve("Applications"))

    @AfterTest
    fun cleanUp() {
        dir.toFile().deleteRecursively()
    }

    @Test
    fun launchArgsQuoteWhatJavaReadsBack() {
        assertEquals(
            "\"-jar\"\n\"C:\\\\Users\\\\A \\\"B\\\"\\\\app.jar\"\n",
            LaunchArgs.text(listOf("-jar", "C:\\Users\\A \"B\"\\app.jar")),
        )
        assertEquals(listOf("--enable-native-access=ALL-UNNAMED", "-Darc.launched=true", "-jar", "a.jar"), LaunchArgs.of(Path.of("a.jar")))
    }

    @Test
    fun theMacBundleRunsTheRuntimeOnLaunchArgs() {
        Files.createDirectories(layout.icons)
        layout.icns.writeText("icns")
        val bundle = MacAppBundle(layout, places)
        val home = dir.resolve("jbr/Contents/Home")
        bundle.write(home, "0.2.0")

        val contents = places.applications.resolve("Arc.app/Contents")
        val plist = contents.resolve("Info.plist").readText()
        assertTrue("<key>CFBundleIdentifier</key><string>dev.chieppa.arc</string>" in plist)
        assertTrue("<key>CFBundleName</key><string>Arc</string>" in plist)
        assertTrue("<key>LSMinimumSystemVersion</key>" in plist)
        assertEquals("icns", contents.resolve("Resources/Arc.icns").readText())
        val script = contents.resolve("MacOS/Arc").readText()
        assertEquals(
            "#!/bin/sh\nexec '${home.resolve("bin/java")}' '-Xdock:name=Arc' '-Xdock:icon=${layout.icns}' '@${layout.launchArgs}' \"\$@\"\n",
            script,
        )
        assertTrue(bundle.exists())

        bundle.write(home, "0.3.0")
        assertTrue("0.3.0" in contents.resolve("Info.plist").readText())
        assertEquals(listOf("Arc.app"), Files.list(places.applications).use { files -> files.map { it.fileName.toString() }.toList() })
        bundle.remove()
        assertFalse(bundle.exists())
    }

    @Test
    fun theStartMenuShortcutRunsJavawOnLaunchArgs() {
        val home = dir.resolve("jbr")
        Files.createDirectories(home.resolve("bin"))
        home.resolve("bin/javaw.exe").writeText("")
        val spec = StartMenuLauncher(layout, places).spec(home)
        assertEquals(home.resolve("bin/javaw.exe"), spec.target)
        assertEquals("\"@${layout.launchArgs}\"", spec.arguments)
        assertEquals(layout.ico, spec.icon)
        assertEquals("dev.chieppa.arc", spec.appId)
    }

    @Test
    fun placesComeFromAppDataAndHome() {
        val places = LauncherPlaces.system(mapOf("APPDATA" to "C:\\R"), Path.of("C:\\H"))
        assertEquals(Path.of("C:\\R", "Microsoft/Windows/Start Menu/Programs"), places.startMenu)
        assertEquals(Path.of("C:\\R", "Microsoft/Internet Explorer/Quick Launch/User Pinned/TaskBar"), places.taskbarPins)
        assertEquals(Path.of("C:\\H", "Applications"), places.applications)
    }
}
