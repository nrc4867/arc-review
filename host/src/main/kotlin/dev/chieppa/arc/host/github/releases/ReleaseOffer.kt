package dev.chieppa.arc.host.github.releases

/** A newer release this machine can update to: its [version], this platform's [jar] and the release's [sums]. */
data class ReleaseOffer(val version: String, val jar: ReleaseAsset, val sums: ReleaseAsset)
