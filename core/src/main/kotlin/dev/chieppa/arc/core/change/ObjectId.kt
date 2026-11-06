package dev.chieppa.arc.core.change

/** A git object's full name, in lowercase hex. */
@JvmInline
value class ObjectId(val value: String) {
    init {
        require(value.length in SHA1_LENGTH..SHA256_LENGTH && value.all { it in '0'..'9' || it in 'a'..'f' }) { "Not an object id: $value" }
    }

    /** Whether this is git's name for no object, all zeros. */
    val isNull: Boolean get() = value.all { it == '0' }

    /** The first characters, as a short sha shows. */
    fun short(length: Int = SHORT_LENGTH): String = value.take(length)

    override fun toString(): String = value

    companion object {
        const val SHA1_LENGTH = 40
        const val SHA256_LENGTH = 64
        const val SHORT_LENGTH = 7
    }
}
