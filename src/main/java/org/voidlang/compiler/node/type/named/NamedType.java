package org.voidlang.compiler.node.type.named;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;
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
public class NamedType extends Type implements NamedTypeEntry {
    /**
     * The name of the type. If it is null, the type is unnamed.
     */
    @Nullable
    private final String name;

    /**
     * Initialize the named type.
     * @param types type tokens
     * @param generics generic arguments
     * @param dimensions array dimensions
     * @param name type name or null
     */
    public NamedType(List<Token> types, List<Token> generics, int dimensions, @Nullable String name) {
        super(types, generics, dimensions);
        this.name = name;
    }
}
