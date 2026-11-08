package dev.chieppa.arc.core.start

/** Where the person put a card: in [column] at position [order], counted from 0. */
data class StartMove(val column: StartColumn, val order: Int)
