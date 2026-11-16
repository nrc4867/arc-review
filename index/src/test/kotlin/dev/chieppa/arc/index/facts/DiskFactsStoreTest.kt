package dev.chieppa.arc.index.facts

import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.syntax.facts.Definition
import dev.chieppa.arc.syntax.facts.FileFacts
import dev.chieppa.arc.syntax.facts.RefKind
import dev.chieppa.arc.syntax.facts.Reference
import dev.chieppa.arc.syntax.facts.SymbolKind
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createTempDirectory
import kotlin.io.path.exists
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class DiskFactsStoreTest {
    private val folder: Path = createTempDirectory("arc-facts")
    private var now = 1_000L

    @AfterTest
    fun cleanUp() {
        folder.toFile().deleteRecursively()
    }

    private fun store(version: String = "v1", maxBytes: Long = FactsStoreLimits.MAX_BYTES) = DiskFactsStore(folder, version, maxBytes) { now }

    private fun blob(n: Int) = ObjectId(n.toString().padStart(BLOB_CHARS, '0'))

    @Test
    fun factsComeBackAsTheyWereWritten() {
        store().write(blob(1), "kotlin", FACTS)
        val read = assertNotNull(store().read(blob(1), "kotlin"))
        assertEquals(FACTS.definitions, read.definitions)
        assertEquals(FACTS.references, read.references)
        assertEquals(listOf(FACTS.packageName, FACTS.imports, FACTS.chars, FACTS.errorChars), listOf(read.packageName, read.imports, read.chars, read.errorChars))
        assertNull(store().read(blob(1), "java"))
    }

    @Test
    fun anotherVersionReadsNothingAndItsFilesGoOnItsFirstWrite() {
        store("v1").write(blob(1), "kotlin", FACTS)
        val next = store("v2")
        assertNull(next.read(blob(1), "kotlin"))
        next.write(blob(2), "kotlin", FACTS)
        assertEquals(false, folder.resolve("v1").exists())
    }

    @Test
    fun theLeastRecentlyUsedGoFirstPastTheLimit() {
        val size = FactsCodec.encode(FACTS).size.toLong()
        val store = store(maxBytes = size * 3)
        for (n in 1..3) {
            now += 1
            store.write(blob(n), "kotlin", FACTS)
        }
        now += 1
        store.read(blob(1), "kotlin")
        now += 1
        store.write(blob(4), "kotlin", FACTS)
        assertNotNull(store.read(blob(1), "kotlin"))
        assertNull(store.read(blob(2), "kotlin"))
        assertNotNull(store.read(blob(4), "kotlin"))
    }

    @Test
    fun aFileThatDoesNotReadIsDropped() {
        store().write(blob(1), "kotlin", FACTS)
        val file = Files.walk(folder).use { walk -> walk.filter { Files.isRegularFile(it) }.findFirst().get() }
        Files.write(file, byteArrayOf(1, 2, 3))
        assertNull(store().read(blob(1), "kotlin"))
        assertEquals(false, file.exists())
    }

    private companion object {
        const val BLOB_CHARS = 40

        val FACTS = FileFacts(
            grammar = "kotlin",
            chars = 120,
            lineCount = 9,
            packageName = "dev.example",
            imports = listOf("dev.example.Token"),
            definitions = listOf(
                Definition("refresh", "refresh", SymbolKind.Function, 10, 17, 2, 4, 0, 60, listOf("token"), "Token", -1, null, null),
                Definition("Session", "Session", SymbolKind.Class, 70, 77, 5, 6, 64, 118, supertypes = listOf("Closeable")),
            ),
            references = listOf(Reference("revoke", RefKind.Call, 30, 36, 3, 8, arity = 1, qualified = true)),
            errorChars = 2,
        )
    }
}
