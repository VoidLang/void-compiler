package org.voidlang.compiler.node.type.name;

/**
 * Represents an entry which may be a {@link ScalarName} or a {@link CompoundName}.
 * The purpose of this class is to be able to hold name groups <strong>recursively</strong>.
 * <p>Examples:</p>
 * <pre> {@code
 *     foo((int, int) point)
 *     bar((int, int) (x, y))
 * } </pre>
 * The code {@code point} will be a {@link ScalarName}, as it does not have any members, and {@code (x, y)}
 * will be a {@link CompoundName}, as it has two members inside.
 * @see ScalarName
 * @see CompoundName
 */
public interface Name {
    /**
     * Indicate, whether this entry is a {@link ScalarName}, so it does not have any nested members.
     * @return true if this type entry is a direct type
     */
    default boolean isScalar() {
        return this instanceof ScalarName;
    }

    /**
     * Indicate, whether this entry is a {@link CompoundName}, so it has nested members only.
     * @return true if this type entry is a group of type entries
     */
    default boolean isCompound() {
        return this instanceof CompoundName;
    }
}
