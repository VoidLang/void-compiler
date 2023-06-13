package org.voidlang.compiler.node.type.named;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.voidlang.compiler.node.type.named.NamedType;
import org.voidlang.compiler.token.Token;

import java.util.List;

/**
 * Represents a lambda type that is a callable anonymous function parameter.
 * <p>Example:</p>
 * <pre> {@code
 *     (void |User| callback) getUserCallback()
 * } </pre>
 * Here {@code void |User| callback} is a named lambda return type of the function {@code getUserCallback}.
 */
@Getter
public class LambdaNamedType extends NamedType {
    /**
     * The held parameter types of the lambda.
     */
    private final List<NamedType> parameters;

    /**
     * Initialize the named type.
     * @param types type tokens
     * @param generics generic arguments
     * @param dimensions array dimensions
     * @param name type name or null
     * @param parameters lambda parameter types
     */
    public LambdaNamedType(List<Token> types, List<Token> generics, int dimensions, @Nullable String name,
                           List<NamedType> parameters) {
        super(types, generics, dimensions, name);
        this.parameters = parameters;
    }
}
