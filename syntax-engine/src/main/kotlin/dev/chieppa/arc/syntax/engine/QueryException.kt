package dev.chieppa.arc.syntax.engine

/** A query that does not compile against its grammar. */
class QueryException(message: String) : RuntimeException(message)
