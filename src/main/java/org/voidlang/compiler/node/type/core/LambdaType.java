package org.voidlang.compiler.node.type.core;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.voidlang.compiler.node.type.parameter.LambdaParameter;
import org.voidlang.llvm.element.IRContext;
import org.voidlang.llvm.element.IRType;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a lambda type that is a callable anonymous function parameter.
 * <p>Example:</p>
 * <pre> {@code
 *     foo(My.Document<User>[] |int a, bool b|)
 * } </pre>
 * Here {@code |int a, bool b|} are the two named parameters of the lambda.
 */
@Getter
public class LambdaType implements Type {
    /**
     * the return type of the lambda type.
     */
    @NotNull
    private final Type type;

    /**
     * The held parameter types of the lambda.
     */
    @NotNull
    private final List<LambdaParameter> parameters;

    public LambdaType(@NotNull Type type, @NotNull List<LambdaParameter> parameters) {
        this.type = type;
        this.parameters = parameters;
    }

    /**
     * Get the string representation of the lambda type.
     * @return lambda type debug information
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(type.toString())
            .append(" |");
        String collect = parameters.stream()
            .map(LambdaParameter::toString)
            .collect(Collectors.joining(", "));
        return builder
            .append(collect)
            .append('|')
            .toString();
    }

    /**
     * Generate an LLVM type for this type wrapper
     * @param context LLVM module context
     * @return type ir code wrapper
     */
    @Override
    public IRType generateType(IRContext context) {
        return null;
    }
}
