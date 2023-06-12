package org.voidlang.compiler.node.method.name;

import lombok.Getter;
import org.voidlang.compiler.node.method.named.NamedType;
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
     * @param named does the type have a name
     * @param name type name or null
     * @param parameters lambda parameter types
     */
    public LambdaNamedType(List<Token> types, List<Token> generics, int dimensions, boolean named, String name,
                           List<NamedType> parameters) {
        super(types, generics, dimensions, named, name);
        this.parameters = parameters;
    }
}
