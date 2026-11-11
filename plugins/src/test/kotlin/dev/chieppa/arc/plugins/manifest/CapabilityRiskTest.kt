package dev.chieppa.arc.plugins.manifest

import kotlin.test.Test
import kotlin.test.assertEquals

class CapabilityRiskTest {
    @Test
    fun readingAndSendingIsFlaggedWithTheCapabilitiesThatMakeIt() {
        val risks = CapabilityRisk.of(Capabilities(panels = true, git = true, network = listOf("api.example.com")))
        assertEquals(listOf(CapabilityRisk(RiskKind.ReadsAndSends, listOf(Capability.Git, Capability.Network("api.example.com")))), risks)
    }

    @Test
    fun fetchingIntoFilesAndRunningProgramsAreFlagged() {
        val capabilities = Capabilities(console = true, files = FileAccess(write = listOf("out")), network = listOf("cdn.example.com"))
        assertEquals(listOf(RiskKind.FetchesIntoFiles, RiskKind.RunsPrograms), CapabilityRisk.of(capabilities).map { it.kind })
    }

    @Test
    fun capabilitiesAloneAreNotRisks() {
        assertEquals(emptyList(), CapabilityRisk.of(Capabilities(panels = true, git = true, files = FileAccess(read = listOf("docs")))))
        assertEquals(emptyList(), CapabilityRisk.of(Capabilities(network = listOf("api.example.com"))))
    }

    @Test
    fun anUpdateAddsOnlyWhatWasNotApproved() {
        val approved = Capabilities(panels = true, network = listOf("a.example.com"))
        val asked = approved.copy(git = true, network = listOf("a.example.com", "b.example.com"))
        assertEquals(listOf(Capability.Git, Capability.Network("b.example.com")), Capability.added(asked, approved))
        assertEquals(emptyList(), Capability.added(approved, asked))
    }
}
