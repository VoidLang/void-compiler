package org.voidlang.compiler.node.type.named;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.voidlang.compiler.node.type.core.ScalarType;
import org.voidlang.compiler.node.type.core.Type;
import org.voidlang.llvm.element.Builder;
import org.voidlang.llvm.element.Value;

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
     * Generate an LLVM instruction for this node
     * @param builder instruction builder for the current context
     * @return node ir code wrapper
     */
    @Override
    public Value generate(Builder builder) {
        return null;
    }
}
