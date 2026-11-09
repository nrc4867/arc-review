package dev.chieppa.arc.host.github.actions

/** What a deployment ships since the environment was last deployed: from [base] to [head], [total] commits, the newest first. */
data class ShipsSince(val base: String, val head: String, val total: Int, val changes: List<ShippedChange>)
