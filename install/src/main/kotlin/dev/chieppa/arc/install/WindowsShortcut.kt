package dev.chieppa.arc.install

import java.io.IOException
import java.lang.foreign.Arena
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.Linker
import java.lang.foreign.MemorySegment
import java.lang.foreign.SymbolLookup
import java.lang.foreign.ValueLayout.ADDRESS
import java.lang.foreign.ValueLayout.JAVA_BYTE
import java.lang.foreign.ValueLayout.JAVA_INT
import java.lang.foreign.ValueLayout.JAVA_SHORT
import java.nio.file.Path

/**
 * Windows shortcuts (`.lnk`) through the shell's own `IShellLinkW`, `IPersistFile` and `IPropertyStore` over FFM:
 * written with their target, arguments, icon and AppUserModelID, and read back for their AppUserModelID and target.
 */
object WindowsShortcut {
    private const val OLE32 = "Ole32"
    private const val COINIT_APARTMENTTHREADED = 2
    private const val CLSCTX_INPROC_SERVER = 1
    private const val RPC_E_CHANGED_MODE = 0x80010106.toInt()
    private const val STGM_READ = 0
    private const val VT_LPWSTR: Short = 31
    private const val PROPVARIANT_SIZE = 16L
    private const val PROPVARIANT_VALUE = 8L
    private const val PROPERTYKEY_SIZE = 20L
    private const val GUID_SIZE = 16L
    private const val MAX_PATH = 32_768
    private const val WIDE = 2

    private const val CLSID_SHELL_LINK = "00021401-0000-0000-C000-000000000046"
    private const val IID_SHELL_LINK = "000214F9-0000-0000-C000-000000000046"
    private const val IID_PERSIST_FILE = "0000010b-0000-0000-C000-000000000046"
    private const val IID_PROPERTY_STORE = "886D8EEB-8CF2-4446-8D02-CDBA1DBDCF99"

    /** `PKEY_AppUserModel_ID`: its format id and property id. */
    private const val APP_ID_FORMAT = "9F4C2855-9F79-4B39-A8D0-E1D42DE1D5F3"
    private const val APP_ID_PROPERTY = 5

    // Places in each interface's table of functions, after IUnknown's QueryInterface (0), AddRef (1), Release (2).
    private const val QUERY_INTERFACE = 0
    private const val RELEASE = 2
    private const val LINK_GET_PATH = 3
    private const val LINK_SET_DESCRIPTION = 7
    private const val LINK_SET_WORKING_DIRECTORY = 9
    private const val LINK_SET_ARGUMENTS = 11
    private const val LINK_SET_ICON_LOCATION = 17
    private const val LINK_SET_PATH = 20
    private const val FILE_LOAD = 5
    private const val FILE_SAVE = 6
    private const val STORE_GET_VALUE = 5
    private const val STORE_SET_VALUE = 6
    private const val STORE_COMMIT = 7

    private val linker = Linker.nativeLinker()
    private val ole32: SymbolLookup by lazy { SymbolLookup.libraryLookup(OLE32, Arena.global()) }

    private val coInitializeEx by lazy { native("CoInitializeEx", FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT)) }
    private val coUninitialize by lazy { native("CoUninitialize", FunctionDescriptor.ofVoid()) }
    private val coCreateInstance by lazy { native("CoCreateInstance", FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, JAVA_INT, ADDRESS, ADDRESS)) }
    private val propVariantClear by lazy { native("PropVariantClear", FunctionDescriptor.of(JAVA_INT, ADDRESS)) }

    /** Writes the shortcut [spec] describes to [file], replacing one there. */
    fun write(spec: ShortcutSpec, file: Path) = withShellLink { arena, link ->
        call(link, LINK_SET_PATH, wide(arena, spec.target.toString()))
        call(link, LINK_SET_ARGUMENTS, wide(arena, spec.arguments))
        call(link, LINK_SET_WORKING_DIRECTORY, wide(arena, spec.workingDirectory.toString()))
        call(link, LINK_SET_DESCRIPTION, wide(arena, spec.description))
        call(link, LINK_SET_ICON_LOCATION, wide(arena, spec.icon.toString()), 0)
        withInterface(arena, link, IID_PROPERTY_STORE) { store ->
            val value = arena.allocate(PROPVARIANT_SIZE)
            value.set(JAVA_SHORT, 0, VT_LPWSTR)
            value.set(ADDRESS, PROPVARIANT_VALUE, wide(arena, spec.appId))
            call(store, STORE_SET_VALUE, appIdKey(arena), value)
            call(store, STORE_COMMIT)
        }
        withInterface(arena, link, IID_PERSIST_FILE) { persist -> call(persist, FILE_SAVE, wide(arena, file.toString()), 1) }
    }

    /** The AppUserModelID of the shortcut [file], or null when it carries none. */
    fun appId(file: Path): String? = withShellLink { arena, link ->
        withInterface(arena, link, IID_PERSIST_FILE) { persist -> call(persist, FILE_LOAD, wide(arena, file.toString()), STGM_READ) }
        withInterface(arena, link, IID_PROPERTY_STORE) { store ->
            val value = arena.allocate(PROPVARIANT_SIZE)
            call(store, STORE_GET_VALUE, appIdKey(arena), value)
            try {
                if (value.get(JAVA_SHORT, 0) != VT_LPWSTR) null else value.get(ADDRESS, PROPVARIANT_VALUE).reinterpret(Long.MAX_VALUE).getString(0, Charsets.UTF_16LE)
            } finally {
                propVariantClear.invokeWithArguments(value)
            }
        }
    }

    /** The program the shortcut [file] runs. */
    fun target(file: Path): String = withShellLink { arena, link ->
        withInterface(arena, link, IID_PERSIST_FILE) { persist -> call(persist, FILE_LOAD, wide(arena, file.toString()), STGM_READ) }
        val buffer = arena.allocate(MAX_PATH.toLong() * WIDE)
        call(link, LINK_GET_PATH, buffer, MAX_PATH, MemorySegment.NULL, 0)
        buffer.getString(0, Charsets.UTF_16LE)
    }

    /** Runs [use] on a new shell link object, with COM started on this thread for the length of the call. */
    private fun <T> withShellLink(use: (Arena, MemorySegment) -> T): T = Arena.ofConfined().use { arena ->
        val started = coInitializeEx.invokeWithArguments(MemorySegment.NULL, COINIT_APARTMENTTHREADED) as Int
        if (started < 0 && started != RPC_E_CHANGED_MODE) throw failed("CoInitializeEx", started)
        try {
            val out = arena.allocate(ADDRESS)
            check("CoCreateInstance", coCreateInstance.invokeWithArguments(guid(arena, CLSID_SHELL_LINK), MemorySegment.NULL, CLSCTX_INPROC_SERVER, guid(arena, IID_SHELL_LINK), out) as Int)
            val link = out.get(ADDRESS, 0)
            try {
                use(arena, link)
            } finally {
                call(link, RELEASE)
            }
        } finally {
            if (started >= 0) coUninitialize.invokeWithArguments()
        }
    }

    /** Runs [use] on [obj]'s interface [iid], released after. */
    private fun <T> withInterface(arena: Arena, obj: MemorySegment, iid: String, use: (MemorySegment) -> T): T {
        val out = arena.allocate(ADDRESS)
        call(obj, QUERY_INTERFACE, guid(arena, iid), out)
        val found = out.get(ADDRESS, 0)
        try {
            return use(found)
        } finally {
            call(found, RELEASE)
        }
    }

    /**
     * Calls function [index] of COM object [obj]'s table with [args] (addresses and ints), failing on an error
     * HRESULT. Release's count is not an HRESULT, so it is never checked.
     */
    private fun call(obj: MemorySegment, index: Int, vararg args: Any): Int {
        val table = obj.reinterpret(ADDRESS.byteSize()).get(ADDRESS, 0).reinterpret(ADDRESS.byteSize() * (index + 1))
        val function = table.getAtIndex(ADDRESS, index.toLong())
        val layouts = args.map { if (it is MemorySegment) ADDRESS else JAVA_INT }
        val handle = linker.downcallHandle(function, FunctionDescriptor.of(JAVA_INT, ADDRESS, *layouts.toTypedArray()))
        val result = handle.invokeWithArguments(listOf(obj) + args) as Int
        if (index != RELEASE) check("COM call $index", result)
        return result
    }

    private fun check(what: String, result: Int) {
        if (result < 0) throw failed(what, result)
    }

    private fun appIdKey(arena: Arena): MemorySegment {
        val key = arena.allocate(PROPERTYKEY_SIZE)
        MemorySegment.copy(guid(arena, APP_ID_FORMAT), 0, key, 0, GUID_SIZE)
        key.set(JAVA_INT, GUID_SIZE, APP_ID_PROPERTY)
        return key
    }

    /** A GUID from its text form, laid out as Windows keeps it. */
    private fun guid(arena: Arena, text: String): MemorySegment {
        val hex = text.replace("-", "")
        val segment = arena.allocate(GUID_SIZE)
        segment.set(JAVA_INT, 0, hex.substring(0, 8).toLong(16).toInt())
        segment.set(JAVA_SHORT, 4, hex.substring(8, 12).toInt(16).toShort())
        segment.set(JAVA_SHORT, 6, hex.substring(12, 16).toInt(16).toShort())
        for (i in 0 until 8) segment.set(JAVA_BYTE, 8L + i, hex.substring(16 + i * 2, 18 + i * 2).toInt(16).toByte())
        return segment
    }

    private fun wide(arena: Arena, text: String): MemorySegment = arena.allocateFrom(text, Charsets.UTF_16LE)

    private fun native(name: String, descriptor: FunctionDescriptor) =
        linker.downcallHandle(ole32.find(name).orElseThrow { UnsatisfiedLinkError("No $name in $OLE32") }, descriptor)

    private fun failed(what: String, result: Int) = IOException("$what failed with 0x%08x".format(result))
}
