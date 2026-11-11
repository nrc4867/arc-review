package dev.chieppa.arc.plugins.view

import dev.chieppa.arc.plugins.protocol.PatchOp
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ViewTreeTest {
    private val label = buildJsonObject { put("label", "Go") }

    @Test
    fun buildsTheTreeTheOpsDescribe() {
        val tree = ViewTree()
        tree.apply(
            listOf(
                PatchOp.Create(1, "Column"),
                PatchOp.Create(2, "Button", label),
                PatchOp.CreateText(3, "hello"),
                PatchOp.Append(1, 2),
                PatchOp.Insert(1, 3, before = 2),
                PatchOp.Append(PatchOp.ROOT, 1),
            ),
        )
        val column = tree.snapshot().single()
        assertEquals(listOf(3, 2), column.children.map { it.id })
        assertEquals("hello", column.children[0].text)
        assertEquals(ViewNode.TEXT, column.children[0].type)
    }

    @Test
    fun updatesMovesAndRemovesNodes() {
        val tree = ViewTree()
        tree.apply(listOf(PatchOp.Create(1, "Row"), PatchOp.Create(2, "Button", label), PatchOp.Create(3, "Text"), PatchOp.Append(1, 2), PatchOp.Append(1, 3), PatchOp.Append(0, 1)))
        tree.apply(listOf(PatchOp.Append(1, 2), PatchOp.SetProps(2, buildJsonObject { put("label", "Stop") })))
        val row = tree.snapshot().single()
        assertEquals(listOf(3, 2), row.children.map { it.id })
        assertEquals("Stop", Props.string(row.children[1].props, "label"))
        tree.apply(listOf(PatchOp.Remove(0, 1)))
        assertTrue(tree.snapshot().isEmpty())
        assertEquals(0, tree.size)
    }

    @Test
    fun opsOnUnknownNodesAreSkipped() {
        val tree = ViewTree()
        tree.apply(listOf(PatchOp.Append(0, 9), PatchOp.SetProps(9, label), PatchOp.Insert(4, 5, 6), PatchOp.Remove(0, 9)))
        assertTrue(tree.snapshot().isEmpty())
    }

    @Test
    fun functionPropsAreHandlers() {
        val props = buildJsonObject { put("onClick", buildJsonObject { put(PatchOp.HANDLER, true) }); put("label", "x") }
        assertTrue(Props.handler(props, "onClick"))
        assertEquals(false, Props.handler(props, "label"))
    }
}
