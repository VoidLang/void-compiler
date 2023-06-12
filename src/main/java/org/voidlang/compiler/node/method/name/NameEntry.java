package org.voidlang.compiler.node.method.name;

/**
 * Represents an entry which may be a {@link Name} or a {@link NameGroup}.
 * The purpose of this class is to be able to hold name groups <strong>recursively</strong>.
 * <p>Examples:</p>
 * <pre> {@code
 *     foo((int, int) point)
 *     bar((int, int) (x, y))
 * } </pre>
 * The code {@code point} will be a {@link Name}, as it does not have any members, and {@code (x, y)}
 * will be a {@link NameGroup}, as it has two members inside.
 * @see Name
 * @see NameGroup
 */
public interface NameEntry {
    /**
     * Indicate, whether this entry is a {@link Name}, so it does not have any nested members.
     * @return true if this type entry is a direct type
     */
    default boolean isName() {
        return this instanceof Name;
    }

    /**
     * Indicate, whether this entry is a {@link NameGroup}, so it has nested members only.
     * @return true if this type entry is a group of type entries
     */
    default boolean isGroup() {
        return this instanceof NameGroup;
    }
}
