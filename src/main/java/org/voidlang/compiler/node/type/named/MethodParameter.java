package org.voidlang.compiler.node.type.named;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.voidlang.compiler.node.type.core.Type;
import org.voidlang.compiler.node.type.name.Name;

/**
 * Represents a method parameter in the Abstract Syntax Tree. Method types are named types with a variadic specifier.
 * <p>Example:</p>
 * <pre> {@code
 *     void onPlayerConnect(Player player)
 * } </pre>
 * Here {@code Player player} is a parameter of the method, {@code Player} is the parameter's type, which is a
 * {@link Type}, and {@code player} is the name of the parameter.
 */
@AllArgsConstructor
@Getter
public class MethodParameter {
    /**
     * The type of the method parameter.
     */
    @NonNull
    private final Type type;

    /**
     * Indicate, whether the method parameter is variadic.
     */
    private final boolean variadic;

    /**
     * The name of the method parameter.
     */
    private final Name name;

    /**
     * Get the string representation of the method parameter.
     * @return method parameter debug information
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(type);
        if (variadic)
            builder.append("...");
        return builder
            .append(' ')
            .append(name)
            .toString();
    }
}
