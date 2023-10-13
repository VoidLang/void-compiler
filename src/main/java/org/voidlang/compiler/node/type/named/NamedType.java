package org.voidlang.compiler.node.type.named;

import org.voidlang.compiler.node.type.core.Type;

/**
 * Represents an entry which may be a {@link NamedScalarType} or a {@link NamedTypeGroup}.
 * The purpose of this class is to be able to hold type groups <strong>recursively</strong>.
 * <p>Examples:</p>
 * <pre> {@code
 *     (float a)
 *     (int a, bool b)
 *     (void |User| callback)
 * } </pre>
 * The code {@code float a} will be a {@link NamedScalarType}, as it does not have any members, and {@code (int a, bool b)}
 * will be a {@link NamedTypeGroup}, as it has two members inside.
 * @see NamedScalarType
 * @see NamedTypeGroup
 * @see NamedLambdaType
 * @see Type
 */
public abstract class NamedType implements Type {
    /**
     * Indicate, whether this entry is a {@link NamedScalarType}, so it does not have any nested members.
     * @return true if this type entry is a direct type
     */
    public boolean isReturnType() {
        return this instanceof NamedScalarType;
    }

    /**
     * Indicate, whether this entry is a {@link NamedTypeGroup}, so it has nested members only.
     * @return true if this type entry is a group of type entries
     */
    public boolean isReturnGroup() {
        return this instanceof NamedTypeGroup;
    }

    /**
     * Indicate, whether this entry is a {@link NamedLambdaType}, so it has nested parameter types.
     * @return true if this type entry is a callable lambda function
     */
    public boolean isLambda() {
        return this instanceof NamedLambdaType;
    }
}
