package dev.chieppa.arc.syntax.docs

/** What a block tag of a doc comment says, whatever the convention spells it. */
enum class DocTagKind {
    /** `@param`, and JSDoc's `@arg`/`@argument`. */
    Param,

    /** KDoc's `@property`. */
    Property,

    /** A type parameter: JavaDoc's `@param <T>`, JSDoc's `@template`, TSDoc's `@typeParam`. */
    TypeParam,

    /** `@return`, `@returns`. */
    Returns,

    /** `@throws`, `@exception`. */
    Throws,

    /** `@see`. */
    See,

    /** `@since`. */
    Since,

    /** JSDoc's `@example` and KDoc's `@sample`. */
    Example,

    /** `@author`. */
    Author,

    /** Any other tag, its name (without `@`) kept in [DocTag.name]. */
    Other,
}
