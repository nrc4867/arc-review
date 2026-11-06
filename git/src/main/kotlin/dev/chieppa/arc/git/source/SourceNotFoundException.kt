package dev.chieppa.arc.git.source

import dev.chieppa.arc.core.source.Source

/** Thrown when a source's commits or branches are not in its repo, or the source needs a host to read. */
class SourceNotFoundException(val source: Source) : Exception("Cannot read ${source.ref}")
