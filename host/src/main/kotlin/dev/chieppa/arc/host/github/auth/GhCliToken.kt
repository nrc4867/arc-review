package dev.chieppa.arc.host.github.auth

import java.io.IOException
import java.util.concurrent.TimeUnit

/** The token of the person's `gh` login, from `gh auth token`; null when `gh` is missing, logged out or slow. */
class GhCliToken(private val program: String = PROGRAM) : TokenSource {
    override fun token(): String? = try {
        val process = ProcessBuilder(program, "auth", "token").redirectError(ProcessBuilder.Redirect.DISCARD).start()
        process.outputStream.close()
        val text = process.inputStream.readAllBytes().toString(Charsets.UTF_8).trim()
        if (!process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            process.destroyForcibly()
            null
        } else {
            text.takeIf { process.exitValue() == 0 && it.isNotEmpty() && it.none(Char::isWhitespace) }
        }
    } catch (_: IOException) {
        null
    }

    override fun origin(): TokenOrigin = TokenOrigin.GhCli

    private companion object {
        const val PROGRAM = "gh"
        const val TIMEOUT_SECONDS = 10L
    }
}
