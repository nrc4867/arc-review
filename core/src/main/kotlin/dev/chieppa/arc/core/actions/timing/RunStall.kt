package dev.chieppa.arc.core.actions.timing

/** A stall in a run: the job's id, the step's number (null for a job stalled in the queue), and seconds over usual. */
data class RunStall(val jobId: Long, val step: Int?, val overSeconds: Long)
