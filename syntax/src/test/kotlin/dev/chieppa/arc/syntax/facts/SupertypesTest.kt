package dev.chieppa.arc.syntax.facts

import dev.chieppa.arc.syntax.grammar.Grammars
import kotlin.test.Test
import kotlin.test.assertEquals

class SupertypesTest {
    private fun supertypes(path: String, text: String): Map<String, List<String>> =
        FactExtractor.extract(Grammars.forPath(path)!!, text).definitions.filter { it.kind.isType }.associate { it.name to it.supertypes }

    @Test
    fun javaTypesKeepWhatTheyExtendAndImplement() {
        val found = supertypes(
            "Stores.java",
            """
            interface Store<K> extends AutoCloseable, java.io.Serializable {}
            abstract class Base implements Store<String> {}
            final class Db extends Base implements Comparable<Db>, Runnable {}
            enum Kind implements Runnable { A }
            record Pair(int a) implements Store<Integer> {}
            """.trimIndent(),
        )
        assertEquals(listOf("AutoCloseable", "Serializable"), found["Store"])
        assertEquals(listOf("Store"), found["Base"])
        assertEquals(setOf("Base", "Comparable", "Runnable"), found.getValue("Db").toSet())
        assertEquals(listOf("Runnable"), found["Kind"])
        assertEquals(listOf("Store"), found["Pair"])
    }

    @Test
    fun kotlinTypesKeepTheirSupertypesWithOrWithoutAConstructorCall() {
        val found = supertypes(
            "Stores.kt",
            """
            interface Store<K> : AutoCloseable
            abstract class Base : Store<String>
            class Db(val url: String) : Base(), Comparable<Db>, com.example.Named
            object Memory : Store<Int>
            class Plain
            """.trimIndent(),
        )
        assertEquals(listOf("AutoCloseable"), found["Store"])
        assertEquals(listOf("Store"), found["Base"])
        assertEquals(setOf("Base", "Comparable", "Named"), found.getValue("Db").toSet())
        assertEquals(listOf("Store"), found["Memory"])
        assertEquals(emptyList(), found["Plain"])
    }

    @Test
    fun typeScriptClassesAndInterfacesKeepTheirSupertypes() {
        val text = """
            interface Store<K> extends Closeable, Named {}
            abstract class Base implements Store<string> {}
            class Db extends Base implements Comparable<Db> {}
        """.trimIndent()
        for (path in listOf("stores.ts", "stores.tsx")) {
            val found = supertypes(path, text)
            assertEquals(listOf("Closeable", "Named"), found["Store"], path)
            assertEquals(listOf("Store"), found["Base"], path)
            assertEquals(setOf("Base", "Comparable"), found.getValue("Db").toSet(), path)
        }
    }
}
