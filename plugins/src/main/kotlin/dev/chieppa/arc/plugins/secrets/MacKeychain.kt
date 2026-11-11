package dev.chieppa.arc.plugins.secrets

import java.io.IOException
import java.lang.foreign.Arena
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.Linker
import java.lang.foreign.MemorySegment
import java.lang.foreign.SymbolLookup
import java.lang.foreign.ValueLayout.ADDRESS
import java.lang.foreign.ValueLayout.JAVA_BYTE
import java.lang.foreign.ValueLayout.JAVA_INT
import java.lang.invoke.MethodHandle
import java.nio.file.Path

/**
 * Plugin secrets in the macOS Keychain, as generic passwords of service [SecretNames.SERVICE] and account
 * [SecretNames.account] in the person's default keychain, through the Security framework's generic password calls.
 */
object MacKeychain : SecretStore {
    private const val SECURITY = "/System/Library/Frameworks/Security.framework/Security"
    private const val CORE_FOUNDATION = "/System/Library/Frameworks/CoreFoundation.framework/CoreFoundation"
    private const val FIND = "SecKeychainFindGenericPassword"
    private const val ADD = "SecKeychainAddGenericPassword"
    private const val MODIFY = "SecKeychainItemModifyAttributesAndData"
    private const val DELETE = "SecKeychainItemDelete"
    private const val FREE = "SecKeychainItemFreeContent"
    private const val RELEASE = "CFRelease"
    private const val OK = 0
    private const val NOT_FOUND = -25300

    private val linker = Linker.nativeLinker()
    private val security: SymbolLookup by lazy { SymbolLookup.libraryLookup(Path.of(SECURITY), Arena.global()) }
    private val coreFoundation: SymbolLookup by lazy { SymbolLookup.libraryLookup(Path.of(CORE_FOUNDATION), Arena.global()) }

    private fun function(library: SymbolLookup, name: String, descriptor: FunctionDescriptor): MethodHandle =
        linker.downcallHandle(library.find(name).orElseThrow { UnsatisfiedLinkError("No $name") }, descriptor)

    private val find by lazy {
        function(security, FIND, FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, ADDRESS, JAVA_INT, ADDRESS, ADDRESS, ADDRESS, ADDRESS))
    }
    private val add by lazy {
        function(security, ADD, FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, ADDRESS, JAVA_INT, ADDRESS, JAVA_INT, ADDRESS, ADDRESS))
    }
    private val modify by lazy { function(security, MODIFY, FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, JAVA_INT, ADDRESS)) }
    private val delete by lazy { function(security, DELETE, FunctionDescriptor.of(JAVA_INT, ADDRESS)) }
    private val free by lazy { function(security, FREE, FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS)) }
    private val release by lazy { function(coreFoundation, RELEASE, FunctionDescriptor.ofVoid(ADDRESS)) }

    override val lasting: Boolean = true

    override fun read(plugin: String, key: String): String? = Arena.ofConfined().use { arena ->
        val length = arena.allocate(JAVA_INT)
        val data = arena.allocate(ADDRESS)
        val status = findItem(arena, plugin, key, length, data, MemorySegment.NULL)
        if (status == NOT_FOUND) return null
        check(FIND, status)
        val secret = data.get(ADDRESS, 0)
        try {
            String(secret.reinterpret(length.get(JAVA_INT, 0).toLong()).toArray(JAVA_BYTE), Charsets.UTF_8)
        } finally {
            free.invokeWithArguments(MemorySegment.NULL, secret)
        }
    }

    override fun write(plugin: String, key: String, value: String) {
        Arena.ofConfined().use { arena ->
            val bytes = value.toByteArray(Charsets.UTF_8)
            val password = arena.allocate(bytes.size.coerceAtLeast(1).toLong())
            MemorySegment.copy(bytes, 0, password, JAVA_BYTE, 0, bytes.size)
            val item = arena.allocate(ADDRESS)
            when (val status = findItem(arena, plugin, key, MemorySegment.NULL, MemorySegment.NULL, item)) {
                NOT_FOUND -> {
                    val (service, serviceLength) = utf8(arena, SecretNames.SERVICE)
                    val (account, accountLength) = utf8(arena, SecretNames.account(plugin, key))
                    check(ADD, add.invokeWithArguments(MemorySegment.NULL, serviceLength, service, accountLength, account, bytes.size, password, MemorySegment.NULL) as Int)
                }
                else -> {
                    check(FIND, status)
                    withItem(item) { check(MODIFY, modify.invokeWithArguments(it, MemorySegment.NULL, bytes.size, password) as Int) }
                }
            }
        }
    }

    override fun delete(plugin: String, key: String) {
        Arena.ofConfined().use { arena ->
            val item = arena.allocate(ADDRESS)
            val status = findItem(arena, plugin, key, MemorySegment.NULL, MemorySegment.NULL, item)
            if (status == NOT_FOUND) return
            check(FIND, status)
            withItem(item) { check(DELETE, delete.invokeWithArguments(it) as Int) }
        }
    }

    private fun findItem(arena: Arena, plugin: String, key: String, length: MemorySegment, data: MemorySegment, item: MemorySegment): Int {
        val (service, serviceLength) = utf8(arena, SecretNames.SERVICE)
        val (account, accountLength) = utf8(arena, SecretNames.account(plugin, key))
        return find.invokeWithArguments(MemorySegment.NULL, serviceLength, service, accountLength, account, length, data, item) as Int
    }

    private inline fun withItem(item: MemorySegment, use: (MemorySegment) -> Unit) {
        val found = item.get(ADDRESS, 0)
        try {
            use(found)
        } finally {
            release.invokeWithArguments(found)
        }
    }

    private fun utf8(arena: Arena, text: String): Pair<MemorySegment, Int> = arena.allocateFrom(text) to text.toByteArray(Charsets.UTF_8).size

    private fun check(call: String, status: Int) {
        if (status != OK) throw IOException("$call failed with $status")
    }
}
