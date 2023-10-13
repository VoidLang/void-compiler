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
     * Indicate, whether this fully qualified name is a "mut" keyword.
     * @return true if this is a "mut" keyword
     */
    public boolean isMut() {
        return types.size() == 1 && types.get(0).is(TokenType.TYPE, "mut");
    }

    /**
     * Indicate, whether this fully qualified name is a "void" keyword.
     * @return true if this is a "void" keyword
     */
    public boolean isVoid() {
        return types.size() == 1 && types.get(0).is(TokenType.TYPE, "void");
    }

    /**
     * Indicate, whether this fully qualified name refers to directly an object.
     * @return true if this accessor does not access nested members
     */
    public boolean isDirect() {
        return types.size() == 1 && !isPrimitive();
    }

    /**
     * Get the direct target of this fully qualified name.
     * @return root name value
     */
    public String getDirect() {
        return types.get(0).getValue();
    }

    /**
     * Indicate, whether this qualified name is a field accessor.
     * @return true if this accessor accesses nested fields
     */
    public boolean isFieldAccess() {
        return types.size() > 1 && types.get(1).is(TokenType.IDENTIFIER);
    }

    /**
     * Get the name of the accessing nested field from the accessor.
     * @return accessed field name
     */
    public String getFieldName() {
        return types.get(1).getValue();
    }

    /**
     * Indicate, whether this qualified name is an index accessor.
     * @return true if this accessor accesses nested elements by index
     */
    public boolean isIndexAccess() {
        return types.size() > 1 && types.get(1).is(TokenType.INTEGER);
    }

    /**
     * Get the index of the accessing nested element from the accessor.
     * @return accessed element index
     */
    public int getIndex() {
        return Integer.parseInt(types.get(1).getValue());
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QualifiedName name = (QualifiedName) o;

        return types.equals(name.types);
    }

    @Override
    public int hashCode() {
        return types.hashCode();
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
