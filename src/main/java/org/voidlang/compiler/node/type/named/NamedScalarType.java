package org.voidlang.compiler.node.type.named;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.voidlang.compiler.node.type.core.ScalarType;
import org.voidlang.compiler.node.type.core.Type;
import org.voidlang.llvm.element.IRBuilder;
import org.voidlang.llvm.element.IRContext;
import org.voidlang.llvm.element.IRType;
import org.voidlang.llvm.element.IRValue;

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
public class NamedScalarType extends NamedType {
    /**
     * The type of the named type.
     */
    @NotNull
    private final Type scalarType;

    /**
     * The name of the type.
     */
    @NotNull
    private final String name;

    /**
     * Indicate, whether the type has a name declared.
     */
    private final boolean named;

    /**
     * Get the string representation of the named scalar type.
     *
     * @return named scalar type debug information
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(scalarType.toString());
        if (named)
            builder.append(' ').append(name);
        return builder.toString();
    }

    /**
     * Generate an LLVM type for this type wrapper
     * @param context LLVM module context
     * @return type ir code wrapper
     */
    @Override
    public IRType generateType(IRContext context) {
        return scalarType.generateType(context);
    }
}