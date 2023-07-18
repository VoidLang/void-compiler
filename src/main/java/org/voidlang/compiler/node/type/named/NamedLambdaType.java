package org.voidlang.compiler.node.type.named;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.voidlang.llvm.element.IRContext;
import org.voidlang.llvm.element.IRType;

import java.util.List;

/**
 * Represents a lambda type that is a callable anonymous function parameter.
 * <p>Example:</p>
 * <pre> {@code
 *     (void |User| callback) getUserCallback()
 * } </pre>
 * Here {@code void |User| callback} is a named lambda return type of the function {@code getUserCallback}.
 */
@AllArgsConstructor
@Getter
public class NamedLambdaType extends NamedType {
    /**
     * the return type of the lambda type.
     */
    @NotNull
    private final NamedType type;

    /**
     * The held parameter types of the lambda.
     */
    @NotNull
    private final List<NamedScalarType> parameters;

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
