package dev.chieppa.arc.plugins.secrets

import java.io.IOException
import java.lang.foreign.Arena
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.Linker
import java.lang.foreign.MemoryLayout
import java.lang.foreign.MemoryLayout.PathElement.groupElement
import java.lang.foreign.MemorySegment
import java.lang.foreign.SymbolLookup
import java.lang.foreign.ValueLayout.ADDRESS
import java.lang.foreign.ValueLayout.JAVA_BYTE
import java.lang.foreign.ValueLayout.JAVA_INT
import java.lang.foreign.ValueLayout.JAVA_LONG
import java.lang.invoke.MethodHandle

/**
 * Secrets in Windows Credential Manager, as generic credentials named by [SecretNames.target], kept for this user on
 * this machine, through `CredWriteW`, `CredReadW`, `CredDeleteW` and `CredFree` in `Advapi32`.
 */
object WindowsCredentials : Keychain {
    private const val LIBRARY = "Advapi32"
    private const val GENERIC = 1
    private const val PERSIST_LOCAL_MACHINE = 2
    private const val NOT_FOUND = 1168
    private const val FAILED = 0
    private const val LAST_ERROR = "GetLastError"
    private const val WRITE = "CredWriteW"
    private const val READ = "CredReadW"
    private const val DELETE = "CredDeleteW"
    private const val FREE = "CredFree"

    // CREDENTIALW's fields.
    private const val FLAGS = "Flags"
    private const val TYPE = "Type"
    private const val TARGET_NAME = "TargetName"
    private const val COMMENT = "Comment"
    private const val LAST_WRITTEN = "LastWritten"
    private const val CREDENTIAL_BLOB_SIZE = "CredentialBlobSize"
    private const val CREDENTIAL_BLOB = "CredentialBlob"
    private const val PERSIST = "Persist"
    private const val ATTRIBUTE_COUNT = "AttributeCount"
    private const val ATTRIBUTES = "Attributes"
    private const val TARGET_ALIAS = "TargetAlias"
    private const val USER_NAME = "UserName"

    private val linker = Linker.nativeLinker()
    private val advapi: SymbolLookup by lazy { SymbolLookup.libraryLookup(LIBRARY, Arena.global()) }
    private val callState = Linker.Option.captureCallState(LAST_ERROR)
    private val stateLayout = Linker.Option.captureStateLayout()

    /** `CREDENTIALW` as laid out on 64-bit Windows. */
    private val credential = MemoryLayout.structLayout(
        JAVA_INT.withName(FLAGS),
        JAVA_INT.withName(TYPE),
        ADDRESS.withName(TARGET_NAME),
        ADDRESS.withName(COMMENT),
        JAVA_LONG.withName(LAST_WRITTEN),
        JAVA_INT.withName(CREDENTIAL_BLOB_SIZE),
        MemoryLayout.paddingLayout(JAVA_INT.byteSize()),
        ADDRESS.withName(CREDENTIAL_BLOB),
        JAVA_INT.withName(PERSIST),
        JAVA_INT.withName(ATTRIBUTE_COUNT),
        ADDRESS.withName(ATTRIBUTES),
        ADDRESS.withName(TARGET_ALIAS),
        ADDRESS.withName(USER_NAME),
    )

    private fun offset(field: String): Long = credential.byteOffset(groupElement(field))

    private fun function(name: String, descriptor: FunctionDescriptor, vararg options: Linker.Option): MethodHandle =
        linker.downcallHandle(advapi.find(name).orElseThrow { UnsatisfiedLinkError("No $name in $LIBRARY") }, descriptor, *options)

    private val credWrite by lazy { function(WRITE, FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT), callState) }
    private val credRead by lazy { function(READ, FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, JAVA_INT, ADDRESS), callState) }
    private val credDelete by lazy { function(DELETE, FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, JAVA_INT), callState) }
    private val credFree by lazy { function(FREE, FunctionDescriptor.ofVoid(ADDRESS)) }

    override fun read(service: String, account: String): String? = Arena.ofConfined().use { arena ->
        val state = arena.allocate(stateLayout)
        val found = arena.allocate(ADDRESS)
        val ok = credRead.invokeWithArguments(state, wide(arena, SecretNames.target(service, account)), GENERIC, 0, found) as Int
        if (ok == FAILED) {
            val error = lastError(state)
            if (error == NOT_FOUND) return null
            throw failed(READ, error)
        }
        val read = found.get(ADDRESS, 0).reinterpret(credential.byteSize())
        try {
            val size = read.get(JAVA_INT, offset(CREDENTIAL_BLOB_SIZE)).toLong()
            val blob = read.get(ADDRESS, offset(CREDENTIAL_BLOB)).reinterpret(size)
            String(blob.toArray(JAVA_BYTE), Charsets.UTF_8)
        } finally {
            credFree.invokeWithArguments(read)
        }
    }

    override fun write(service: String, account: String, value: String) {
        Arena.ofConfined().use { arena ->
            val bytes = value.toByteArray(Charsets.UTF_8)
            val blob = arena.allocate(bytes.size.coerceAtLeast(1).toLong())
            MemorySegment.copy(bytes, 0, blob, JAVA_BYTE, 0, bytes.size)
            val written = arena.allocate(credential)
            written.set(JAVA_INT, offset(TYPE), GENERIC)
            written.set(ADDRESS, offset(TARGET_NAME), wide(arena, SecretNames.target(service, account)))
            written.set(JAVA_INT, offset(CREDENTIAL_BLOB_SIZE), bytes.size)
            written.set(ADDRESS, offset(CREDENTIAL_BLOB), blob)
            written.set(JAVA_INT, offset(PERSIST), PERSIST_LOCAL_MACHINE)
            written.set(ADDRESS, offset(USER_NAME), wide(arena, account))
            val state = arena.allocate(stateLayout)
            if (credWrite.invokeWithArguments(state, written, 0) as Int == FAILED) throw failed(WRITE, lastError(state))
        }
    }

    override fun delete(service: String, account: String) {
        Arena.ofConfined().use { arena ->
            val state = arena.allocate(stateLayout)
            if (credDelete.invokeWithArguments(state, wide(arena, SecretNames.target(service, account)), GENERIC, 0) as Int != FAILED) return
            val error = lastError(state)
            if (error != NOT_FOUND) throw failed(DELETE, error)
        }
    }

    private fun wide(arena: Arena, text: String): MemorySegment = arena.allocateFrom(text, Charsets.UTF_16LE)

    private fun failed(call: String, error: Int) = IOException("$call failed with $error")

    private fun lastError(state: MemorySegment): Int = state.get(JAVA_INT, stateLayout.byteOffset(groupElement(LAST_ERROR)))
}
