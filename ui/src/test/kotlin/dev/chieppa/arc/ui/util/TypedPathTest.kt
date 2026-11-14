package dev.chieppa.arc.ui.util

import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TypedPathTest {
    private val home = Path.of(System.getProperty("java.io.tmpdir")).toAbsolutePath().resolve("home")

    @Test
    fun aTildeIsTheHomeFolder() {
        assertEquals(home, TypedPath.read("~", home))
        assertEquals(home.resolve("IdeaProjects/arc"), TypedPath.read("~/IdeaProjects/arc", home))
        assertEquals(home.resolve("IdeaProjects/arc"), TypedPath.read("  '~/IdeaProjects/arc'  ", home))
    }

    @Test
    fun aBackslashAfterTheTildeIsASeparatorOnWindowsOnly() {
        assertEquals(home.resolve("IdeaProjects"), TypedPath.read("~\\IdeaProjects", home, windows = true))
        assertNull(TypedPath.read("~\\IdeaProjects", home, windows = false))
    }

    @Test
    fun anAbsolutePathIsTakenAsItIsWithoutItsQuotes() {
        val folder = home.resolve("notes").toString()
        assertEquals(Path.of(folder), TypedPath.read(folder, home))
        assertEquals(Path.of(folder), TypedPath.read("\"$folder\"\n", home))
    }

    @Test
    fun otherRelativePathsAreNoPath() {
        assertNull(TypedPath.read("IdeaProjects/arc", home))
        assertNull(TypedPath.read("~fennec/arc", home))
        assertNull(TypedPath.read("token", home))
        assertNull(TypedPath.read("   ", home))
        assertNull(TypedPath.read("\"\"", home))
    }
}
