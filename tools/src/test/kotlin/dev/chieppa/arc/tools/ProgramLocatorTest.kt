package dev.chieppa.arc.tools

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals

class ProgramLocatorTest {
    private val root: Path = createTempDirectory("locator")

    private fun program(folder: String, name: String): Path {
        val dir = Files.createDirectories(root.resolve(folder))
        return Files.createFile(dir.resolve(name)).also { it.toFile().setExecutable(true) }
    }

    @Test
    fun aProgramOnThePathIsFoundThere() {
        val gh = program("bin", "gh")
        val locator = ProgramLocator(mapOf("PATH" to root.resolve("bin").toString()), root.toString(), windows = false)
        assertEquals(gh.toString(), locator.find("gh"))
    }

    @Test
    fun aProgramMissingFromAShortPathIsFoundInTheHomeFolders() {
        val gh = program(".local/bin", "gh")
        val locator = ProgramLocator(mapOf("PATH" to root.resolve("empty").toString()), root.toString(), windows = false)
        assertEquals(gh.toString(), locator.find("gh"))
    }

    @Test
    fun onWindowsTheExtensionsAreTried() {
        val gh = program("bin", "gh.exe")
        val locator = ProgramLocator(mapOf("Path" to root.resolve("bin").toString(), "PATHEXT" to ".COM;.EXE"), root.toString(), windows = true)
        assertEquals(gh.toString(), locator.find("gh"))
    }

    @Test
    fun aProgramFoundNowhereIsLeftToTheSystem() {
        val locator = ProgramLocator(mapOf("PATH" to root.toString()), root.toString(), windows = false)
        assertEquals("no-such-program", locator.find("no-such-program"))
    }
}
