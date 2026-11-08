package dev.chieppa.arc.index.resolve

import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.index.IndexedFile
import dev.chieppa.arc.index.symbols.SymbolTable
import dev.chieppa.arc.index.symbols.UseSite
import dev.chieppa.arc.syntax.facts.FactExtractor
import dev.chieppa.arc.syntax.grammar.Grammars
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ResolverTest {
    private fun file(path: String, text: String, repo: String = "app"): IndexedFile {
        val grammar = Grammars.forPath(path)!!
        return IndexedFile(RepoName(repo), "r1", path, ObjectId("0".repeat(40)), grammar, FactExtractor.extract(grammar, text))
    }

    private val list = file("src/main/kotlin/ui/List.kt", "package ui\n\nclass List {\n    fun add(item: String, at: Int) {}\n}\n")
    private val tree = file("src/main/kotlin/ui/Tree.kt", "package ui\n\nclass Tree {\n    fun add(item: String) {}\n}\n")
    private val other = file("src/main/kotlin/data/Store.kt", "package data\n\nfun add(item: String) {}\n")
    private val testHelper = file("src/test/kotlin/ui/Helpers.kt", "package ui\n\nfun add(item: String) {}\n")

    private fun resolve(caller: IndexedFile, name: String): Resolution {
        val table = SymbolTable(sequenceOf(list, tree, other, testHelper, caller))
        val use = UseSite(caller, caller.facts.references.first { it.name == name })
        return Resolver(table).resolve(use)
    }

    @Test
    fun argumentCountPicksBetweenMembersCalledOnAReceiver() {
        val caller = file("src/main/kotlin/ui/Screen.kt", "package ui\n\nfun show(list: List) { list.add(\"a\", 2) }\n")
        val resolution = resolve(caller, "add")
        assertEquals(listOf("src/main/kotlin/ui/List.kt"), resolution.confident.map { it.site.file.path })
        assertTrue(resolution.filtered.any { it.site.file.path == "src/main/kotlin/ui/Tree.kt" })
    }

    @Test
    fun anUnqualifiedCallCannotReachAnotherClassMemberAnotherPackageOrTestCode() {
        val caller = file("src/main/kotlin/ui/Screen.kt", "package ui\n\nfun show() { add(\"a\") }\n")
        val resolution = resolve(caller, "add")
        assertTrue(resolution.confident.isEmpty())
        assertEquals(4, resolution.filtered.size)
    }

    @Test
    fun anImportBringsATopLevelFunctionFromAnotherPackageIntoScope() {
        val caller = file("src/main/kotlin/ui/Screen.kt", "package ui\n\nimport data.add\n\nfun show() { add(\"a\") }\n")
        assertEquals(listOf("src/main/kotlin/data/Store.kt"), resolve(caller, "add").confident.map { it.site.file.path })
    }

    @Test
    fun aRelativeImportPicksTheTypeScriptModule() {
        assertTrue(ModulePaths.resolvesTo("src/hooks/useSession.ts", "../api/types", "src/api/types.ts"))
        assertTrue(ModulePaths.resolvesTo("src/app/App.tsx", "./ui", "src/app/ui/index.tsx"))
        assertTrue(!ModulePaths.resolvesTo("src/app/App.tsx", "react", "src/react.ts"))
    }
}
