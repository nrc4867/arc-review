package dev.chieppa.arc.build.natives

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.workers.WorkParameters

/** What [CompileLibrary] compiles: one library for one target. */
interface CompileLibraryParameters : WorkParameters {
    val zig: RegularFileProperty
    val target: Property<String>
    val flags: ListProperty<String>
    val includes: ListProperty<String>
    val sources: ListProperty<String>
    val output: RegularFileProperty
    val scratch: DirectoryProperty
    val zigCache: DirectoryProperty
}
