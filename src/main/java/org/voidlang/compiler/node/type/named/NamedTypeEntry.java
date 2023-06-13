package org.voidlang.compiler.node.type.named;

/**
 * Represents an entry which may be a {@link NamedType} or a {@link NamedTypeGroup}.
 * The purpose of this class is to be able to hold type groups <strong>recursively</strong>.
 * <p>Examples:</p>
 * <pre> {@code
 *     (float a) foo()
 *     (int a, bool b) bar()
 * } </pre>
 * The code {@code float a} will be a {@link NamedType}, as it does not have any members, and {@code (int a, bool b)}
 * will be a {@link NamedTypeGroup}, as it has two members inside.
 * @see NamedType
 * @see NamedTypeGroup
 */
public interface NamedTypeEntry {
    /**
     * Indicate, whether this entry is a {@link NamedType}, so it does not have any nested members.
     * @return true if this type entry is a direct type
     */
    default boolean isReturnType() {
        return this instanceof NamedType;
    }

    /**
     * Indicate, whether this entry is a {@link NamedTypeGroup}, so it has nested members only.
     * @return true if this type entry is a group of type entries
     */
    default boolean isReturnGroup() {
        return this instanceof NamedTypeGroup;
    }
}
