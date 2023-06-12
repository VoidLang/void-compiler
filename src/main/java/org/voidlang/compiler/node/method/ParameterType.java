package org.voidlang.compiler.node.method;

import lombok.Getter;
import org.voidlang.compiler.node.method.type.Type;
import org.voidlang.compiler.token.Token;

import java.util.List;

/**
 * Represents a function parameter type.
 */
@Getter
public class ParameterType extends Type {
    /**
     * Indicate, whether the function parameter is variadic.
     */
    private final boolean variadic;

    /**
     * The name of the parameter.
     */
    private final String name;

    /**
     * Initialize the parameter type.
     * @param types type tokens
     * @param generics generic arguments
     * @param dimensions array dimensions
     * @param variadic is the parameter variadic
     * @param name parameter name
     */
    public ParameterType(List<Token> types, List<Token> generics, int dimensions, boolean variadic, String name) {
        super(types, generics, dimensions);
        this.variadic = variadic;
        this.name = name;
    }
}
