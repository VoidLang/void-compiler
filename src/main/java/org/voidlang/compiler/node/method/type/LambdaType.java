package org.voidlang.compiler.node.method.type;

import lombok.Getter;
import org.voidlang.compiler.node.method.named.NamedType;
import org.voidlang.compiler.token.Token;

import java.util.List;

/**
 * Represents a lambda type that is a callable anonymous function parameter.
 * <p>Example:</p>
 * <pre> {@code
 *     foo(My.Document<User>[] |int a, bool b|)
 * } </pre>
 * Here {@code |int a, bool b|} are the two named parameters of the lambda.
 */
@Getter
public class LambdaType extends Type {
    /**
     * The held parameter types of the lambda.
     */
    private final List<NamedType> parameters;

    /**
     * Initialize the lambda type.
     * @param types lambda return type
     * @param generics lambda return type generics
     * @param dimensions lambda return type dimensions
     * @param parameters lambda parameter types
     */
    public LambdaType(List<Token> types, List<Token> generics, int dimensions, List<NamedType> parameters) {
        super(types, generics, dimensions);
        this.parameters = parameters;
    }
}
