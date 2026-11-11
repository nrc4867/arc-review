package dev.chieppa.arc.plugins.starter

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class StarterNameTest {
    @Test
    fun aNameMakesALowercaseDashedId() {
        assertEquals(StarterName("Todo Counter", "todo-counter"), StarterName.of("  Todo Counter "))
        assertEquals("spec-view-2", StarterName.of("spec.view 2")?.id)
        assertEquals("a-b", StarterName.of("A__B")?.id)
    }

    @Test
    fun aNameWithoutLettersOrWithQuotesIsRefused() {
        assertNull(StarterName.of(" -- "))
        assertNull(StarterName.of("say \"hi\""))
        assertNull(StarterName.of("a/b"))
        assertNull(StarterName.of(""))
    }
}
