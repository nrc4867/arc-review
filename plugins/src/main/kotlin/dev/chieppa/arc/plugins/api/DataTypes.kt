package dev.chieppa.arc.plugins.api

import dev.chieppa.arc.plugins.protocol.FileInfo
import dev.chieppa.arc.plugins.protocol.GrepHit
import dev.chieppa.arc.plugins.protocol.NoteInfo
import dev.chieppa.arc.plugins.protocol.NoteKind
import dev.chieppa.arc.plugins.protocol.NoteSide
import dev.chieppa.arc.plugins.protocol.RepoInfo
import dev.chieppa.arc.plugins.protocol.SettingType
import dev.chieppa.arc.plugins.protocol.SourceInfo
import kotlinx.serialization.descriptors.SerialDescriptor

/** The protocol's data types plugins see, as the generated types and the reference write them. */
object DataTypes {
    /** The enums written as unions of their names. */
    val unions: List<SerialDescriptor> = listOf(SettingType.serializer().descriptor, NoteKind.serializer().descriptor, NoteSide.serializer().descriptor)

    /** The data classes written as interfaces. */
    val interfaces: List<SerialDescriptor> = listOf(
        RepoInfo.serializer().descriptor,
        SourceInfo.serializer().descriptor,
        FileInfo.serializer().descriptor,
        GrepHit.serializer().descriptor,
        NoteInfo.serializer().descriptor,
    )
}
