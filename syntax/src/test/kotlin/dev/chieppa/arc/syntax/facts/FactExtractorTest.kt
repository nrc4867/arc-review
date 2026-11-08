package dev.chieppa.arc.syntax.facts

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FactExtractorTest {
    @Test
    fun kotlinDefinitionsCarryParametersTypesAndContainers() {
        val facts = Fixtures.facts("CustomerSync.kt")
        assertEquals("com.example.orders", facts.packageName)
        assertEquals(listOf("javax.sql.DataSource"), facts.imports)
        val place = facts.definitions.single { it.name == "placeOrder" }
        assertEquals(SymbolKind.Function, place.kind)
        assertEquals(listOf("repository", "ref", "lines"), place.parameters)
        assertEquals("Long", place.type)
        assertEquals("CustomerSync", place.container)
        val sync = facts.definitions.single { it.name == "sync" }
        assertEquals(listOf("batchSize", "dryRun"), sync.parameters)
        assertEquals("Map<String, Int>", sync.type)
        assertTrue(facts.definitions.single { it.name == "sql" }.isLocal)
        assertEquals(SymbolKind.Property, facts.definitions.single { it.name == "dataSource" }.kind)
    }

    @Test
    fun callsCountTheirOwnArgumentsAndKnowWhetherTheyAreQualified() {
        val facts = Fixtures.facts("CustomerSync.kt")
        val setInt = facts.references.first { it.name == "setInt" }
        assertEquals(RefKind.Call, setInt.kind)
        assertEquals(2, setInt.arity)
        assertTrue(setInt.qualified)
        val buildMap = facts.references.first { it.name == "buildMap" }
        assertEquals(1, buildMap.arity)
        assertTrue(!buildMap.qualified)
        assertEquals(RefKind.Type, facts.references.first { it.name == "CustomerRef" }.kind)
    }

    @Test
    fun definitionNamesAreNotReferences() {
        val facts = Fixtures.facts("CustomerSync.kt")
        val place = facts.definitions.single { it.name == "placeOrder" }
        assertNull(facts.referenceAt(place.nameStart))
        assertEquals(place, facts.definitionAt(place.nameStart + 1))
    }

    @Test
    fun javaConstructorsMethodsAndSqlInStrings() {
        val facts = Fixtures.facts("OrderRepository.java")
        assertEquals(SymbolKind.Constructor, facts.definitions.single { it.kind == SymbolKind.Constructor }.kind)
        assertEquals(listOf("customerRef", "lines"), facts.definitions.single { it.name == "placeOrder" }.parameters)
        assertEquals(3, facts.references.single { it.name == "OrderSummary" && it.kind == RefKind.Call }.arity)
        val sql = facts.references.filter { it.kind == RefKind.SqlString }.map { "${it.schema}.${it.name}" }
        assertTrue("sales.usp_placeorder" in sql && "sales.usp_searchorders" in sql && "sales.ufn_customerbalance" in sql, sql.toString())
    }

    @Test
    fun typeScriptInterfacesMembersEnumsAndFunctions() {
        val facts = Fixtures.facts("session.ts")
        assertEquals(listOf("./ids", "./client"), facts.imports)
        assertEquals(SymbolKind.Interface, facts.definitions.single { it.name == "TokenPair" }.kind)
        assertEquals("TokenPair", facts.definitions.single { it.name == "familyId" }.container)
        assertEquals(listOf("force", "attempt"), facts.definitions.single { it.name == "refresh" }.parameters)
        assertEquals("Promise<TokenPair>", facts.definitions.single { it.name == "refresh" }.type)
        assertEquals(SymbolKind.Constant, facts.definitions.single { it.name == "Revoked" }.kind)
        assertEquals(SymbolKind.Type, facts.definitions.single { it.name == "SessionState" }.kind)
        assertEquals(SymbolKind.Function, facts.definitions.single { it.name == "useSession" }.kind)
    }

    @Test
    fun sqlNamesKeepTheirSchemaAndCompareInLowerCase() {
        val tables = Fixtures.facts("Orders.sql")
        val customer = tables.definitions.single { it.kind == SymbolKind.Table && it.name == "customer" }
        assertEquals("Customer", customer.label)
        assertEquals("sales", customer.container)
        assertEquals("customer", tables.definitions.first { it.kind == SymbolKind.Column }.container)
        val proc = Fixtures.facts("usp_PlaceOrder.sql").definitions.single { it.kind == SymbolKind.Procedure }
        assertEquals("usp_placeorder", proc.name)
        assertEquals(4, proc.arity)
    }
}
