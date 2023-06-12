package org.voidlang.compiler.node.method.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.voidlang.compiler.node.method.type.TypeEntry;
import org.voidlang.compiler.token.Token;

import java.util.List;

/**
 * Represents a type use in the Void syntax that has a type token, generic arguments and array dimensions.
 * <p>Example:</p>
 * <pre> {@code
 *     MyCollection.Document<User>[] documents
 * } </pre>
 * Here {@code MyCollection.Document} is the type token, {@code <User>} is the generic token, and
 * {@code []} is a one-dimensional array specifier.
 */
@Getter
@AllArgsConstructor
public class Type implements TypeEntry {
    /**
     * The type tokens of the type. Having multiple type tokens means that we are accessing
     * an inner element of a parent type. Eg: UserService.User
     */
    private final List<Token> types;

    /**
     * The generic argument tokens of the type.
     */
    private final List<Token> generics;

    /**
     * Get dimensions of the type. Each open-close square bracket pair increases the dimensions by one.
     */
    private final int dimensions;
}
