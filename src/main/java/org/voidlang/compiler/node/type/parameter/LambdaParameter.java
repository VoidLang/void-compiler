package org.voidlang.compiler.node.type.parameter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.voidlang.compiler.node.type.core.Type;
import org.voidlang.compiler.node.type.name.Name;
import org.voidlang.compiler.node.type.pointer.Referencing;
import org.voidlang.llvm.element.IRContext;
import org.voidlang.llvm.element.IRType;

/**
 * Represents a lambda parameter in the Abstract Syntax Tree. Lambdas are callable anonymous function.
 * <p>Example:</p>
 * <pre> {@code
 *     void onPlayerConnect(int |Player| callback)
 * } </pre>
 * Here {@code int} is the return type of the lambda, {@code |Player|} is the parameter list, which includes a
 * {@link LambdaParameter} of the type {@code Player}, finally {@code callback} is the name of the lambda.
 */
@AllArgsConstructor
@Getter
public class LambdaParameter implements Type {
    @NonNull
    private final Referencing referencing;

    /**
     * The type of the lambda parameter.
     */
    @NotNull
    private final Type type;

    /**
     * Indicate, whether the lambda parameter is variadic.
     */
    private final boolean variadic;

    /**
     * The name of the lambda parameter.
     */
    @Nullable
    private final Name name;

    /**
     * Indicate, whether the lambda parameter has a named declared
     */
    private final boolean named;

    /**
     * Get the string representation of the lambda type.
     * @return lambda type debug information
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(type.toString());
        if (variadic)
            builder.append("...");
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
        throw new IllegalStateException("Generating type for " + getClass().getSimpleName() + " is not implemented yet.");
    }
}
