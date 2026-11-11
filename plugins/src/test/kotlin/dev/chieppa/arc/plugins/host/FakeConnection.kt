package dev.chieppa.arc.plugins.host

import dev.chieppa.arc.plugins.protocol.Envelope
import dev.chieppa.arc.plugins.protocol.ProtocolJson
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel

/** A plugin process played by a test: what Arc sends lands in [sent], and the test writes lines and ends it. */
class FakeConnection : LineConnection {
    val sent = Channel<Envelope>(Channel.UNLIMITED)

    /** Every line Arc sent, envelope or not. */
    val raw = mutableListOf<String>()
    private val out = Channel<String>(Channel.UNLIMITED)
    private val err = Channel<String>(Channel.UNLIMITED)
    private val exit = CompletableDeferred<Int>()
    var killed = false
        private set

    override val lines: ReceiveChannel<String> get() = out
    override val logs: ReceiveChannel<String> get() = err

    override suspend fun send(line: String) {
        raw += line
        ProtocolJson.decode(line)?.let { sent.send(it) }
    }

    override suspend fun awaitExit(): Int = exit.await()

    override fun kill() {
        killed = true
        end(KILLED)
    }

    fun write(envelope: Envelope) {
        out.trySend(ProtocolJson.encode(envelope))
    }

    fun log(line: String) {
        err.trySend(line)
    }

    fun end(code: Int) {
        exit.complete(code)
        out.close()
        err.close()
    }

    companion object {
        const val KILLED = 137
    }
}
