package org.voidlang.compiler.node.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.voidlang.compiler.token.Token;
import org.voidlang.compiler.token.TokenType;

import java.util.List;

/**
 * Represents a type token holder of a fully qualified type name.
 * Type tokens are connected with dot symbols.
 * <p>Example:</p>
 * <pre> {@code
 *     My.Type.Inner.Element
 * } </pre>
 */
@AllArgsConstructor
@Getter
public class QualifiedName {
    /**
     * The type tokens of the type. Having multiple type tokens means that we are accessing
     * an inner element of a parent type. Eg: UserService.User
     */
    @NotNull
    private final List<Token> types;

    /**
     * Indicate, whether this fully qualified name is a primitive type.
     * @return true if this is a primitive type
     */
    public boolean isPrimitive() {
        return types.size() == 1 && types.get(0).is(TokenType.TYPE);
    }
}
