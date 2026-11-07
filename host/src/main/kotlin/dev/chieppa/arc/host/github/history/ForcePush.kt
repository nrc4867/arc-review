package dev.chieppa.arc.host.github.history

import java.time.Instant

/** A force push of a pull request's head from [before] to [after], at [at]. GitHub may no longer know [before]. */
data class ForcePush(val before: String?, val after: String?, val at: Instant)
