package org.voidlang.compiler.node.type.named;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.voidlang.compiler.node.type.core.ScalarType;

/**
 * Represents a type that optionally has a unique name given.
 * <p>Example:</p>
 * <pre> {@code
 *     (bool success, string msg) login()
 * } </pre>
 * Here {@code bool success} and {@code string msg} are two named types of a tuple return type.
 * <br>
 * {@code bool} and {@code string} are the type of the {@link NamedScalarType}, {@code success} and
 * {@code msg} are their names.
 * @see ScalarType
 */
@AllArgsConstructor
@Getter
public class NamedScalarType implements NamedType {
    /**
     * The type of the named type.
     */
    @NotNull
    private final ScalarType scalarType;

    /**
     * The name of the type. If it is null, the type is unnamed.
     */
    @Nullable
    private final String name;
}
