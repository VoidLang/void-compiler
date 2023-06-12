package org.voidlang.compiler.node.method.type;

/**
 * Represents an entry which may be a {@link Type} or a {@link TypeGroup}.
 * The purpose of this class is to be able to hold type groups <strong>recursively</strong>.
 * <p>Examples:</p>
 * <pre> {@code
 *     float foo()
 *     (int, bool) bar()
 * } </pre>
 * The code {@code float} will be a {@link Type}, as it does not have any members, and {@code (int, bool)}
 * will be a {@link TypeGroup}, as it has two members inside.
 * @see Type
 * @see TypeGroup
 */
public interface TypeEntry {
    /**
     * Indicate, whether this entry is a {@link Type}, so it does not have any nested members.
     * @return true if this type entry is a direct type
     */
    default boolean isType() {
        return this instanceof Type;
    }

    /**
     * Indicate, whether this entry is a {@link TypeGroup}, so it has nested members only.
     * @return true if this type entry is a group of type entries
     */
    default boolean isGroup() {
        return this instanceof TypeGroup;
    }
}
