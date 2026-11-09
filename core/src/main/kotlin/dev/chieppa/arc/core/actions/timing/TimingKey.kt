package dev.chieppa.arc.core.actions.timing

/** Which job a timing history is for: the job [job] of the workflow in file [workflowPath] of [repo] (`owner/name`). */
data class TimingKey(val repo: String, val workflowPath: String, val job: String) {
    override fun toString(): String = "${repo.lowercase()}|$workflowPath|$job"

    companion object {
        /** The key [toString] wrote, or null when it is not one. */
        fun parse(text: String): TimingKey? {
            val parts = text.split('|')
            return if (parts.size == 3 && parts.none { it.isEmpty() }) TimingKey(parts[0], parts[1], parts[2]) else null
        }
    }
}
