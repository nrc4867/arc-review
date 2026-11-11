package dev.chieppa.arc.plugins.manifest

/** A [kind] of risk and the [capabilities] that make it, which the plugin's row highlights. */
data class CapabilityRisk(val kind: RiskKind, val capabilities: List<Capability>) {
    companion object {
        /** The risky combinations [capabilities] declares. */
        fun of(capabilities: Capabilities): List<CapabilityRisk> {
            val all = Capability.of(capabilities)
            val network = all.filterIsInstance<Capability.Network>()
            val reads = all.filter { it == Capability.Git || it is Capability.ReadFiles }
            val writes = all.filterIsInstance<Capability.WriteFiles>()
            return buildList {
                if (network.isNotEmpty() && reads.isNotEmpty()) add(CapabilityRisk(RiskKind.ReadsAndSends, reads + network))
                if (network.isNotEmpty() && writes.isNotEmpty()) add(CapabilityRisk(RiskKind.FetchesIntoFiles, network + writes))
                if (capabilities.console) add(CapabilityRisk(RiskKind.RunsPrograms, listOf(Capability.Console)))
            }
        }
    }
}
