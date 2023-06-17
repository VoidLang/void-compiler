package org.voidlang.compiler.node.type;

import dev.inventex.octa.console.ConsoleFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.voidlang.compiler.node.type.core.Type;
import org.voidlang.compiler.token.Token;
import org.voidlang.compiler.token.TokenType;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    /**
     * Get the primitive value of this qualified name.
     * @return primitive token value
     */
    public String getPrimitive() {
        if (!isPrimitive())
            throw new IllegalStateException("QualifiedName is not primitive: " + types);
        return types.get(0).getValue();
    }

    /**
     * Indicate, whether this fully qualified name is a "let" keyword.
     * @return true if this is a "let" keyword
     */
    public boolean isLet() {
        return types.size() == 1 && types.get(0).is(TokenType.TYPE, "let");
    }

    /**
     * Get the string representation of the qualified name.
     * @return name debug information
     */
    @Override
    public String toString() {
        return (isPrimitive() ? ConsoleFormat.LIGHT_RED : ConsoleFormat.YELLOW)
            + types.stream()
            .map(Token::getValue)
            .collect(Collectors.joining("."));
    }

    /**
     * Create a primitive fully qualified type name wrapper for the specified type.
     * @param type primitive type name
     * @return primitive type wrapper
     */
    public static QualifiedName primitive(String type) {
        return new QualifiedName(Collections.singletonList(Token.of(TokenType.TYPE, type)));
    }
}
