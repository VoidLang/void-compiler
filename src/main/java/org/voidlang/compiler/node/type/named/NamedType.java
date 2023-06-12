package org.voidlang.compiler.node.type.named;

import lombok.Getter;
import org.voidlang.compiler.node.type.core.Type;
import org.voidlang.compiler.token.Token;

import java.util.List;

/**
 * Represents a type that optionally has a unique name given.
 * <p>Example:</p>
 * <pre> {@code
 *     (bool success, string msg) login()
 * } </pre>
 * Here {@code bool success} and {@code string msg} are two named types of a tuple return type.
 * <br>
 * {@code bool} and {@code string} are the type of the {@link NamedType}, {@code success} and
 * {@code msg} are their names.
 * @see Type
 */
@Getter
public class NamedType extends Type implements ReturnTypeEntry {
    /**
     * Indicate, whether the type as a name declared.
     */
    private final boolean named;

    /**
     * The name of the type. It is null if <code>named</code> is false.
     */
    private final String name;

    /**
     * Initialize the named type.
     * @param types type tokens
     * @param generics generic arguments
     * @param dimensions array dimensions
     * @param named does the type have a name
     * @param name type name or null
     */
    public NamedType(List<Token> types, List<Token> generics, int dimensions, boolean named, String name) {
        super(types, generics, dimensions);
        this.named = named;
        this.name = name;
    }
}
