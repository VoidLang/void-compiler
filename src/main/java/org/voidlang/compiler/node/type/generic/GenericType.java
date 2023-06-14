package org.voidlang.compiler.node.type.generic;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.voidlang.compiler.node.type.QualifiedName;
import org.voidlang.compiler.node.type.core.Type;

/**
 * Represents a generic type declaration for a method or a type.
 * A generic type has a name, that is accessible in the type's scope.
 * Type generics are accessible throughout the whole type.
 * Generic shadowing is also possible. Nested generic types override their outer types.
 * <p>Examples:</p>
 * If a default value is assigned for a generic type, the default value will be used
 * if the generic type is not specified explicitly.
 * <pre> {@code
 *     T serialize<T = JsonObject>(string json)
 * } </pre>
 * If there is no default value specified, and the generic type is not specified explicitly,
 * the compiler will try to infer generic arguments by checking how they are used.
 * <pre> {@code
 *     List<T> asList(T... elements)
 * } </pre>
 * Generic types can be specified explicitly as well, so all values of the
 * generics must be in instance of the declared type.
 */
@AllArgsConstructor
@Getter
public class GenericType {
    /**
     * The name of the generic type.
     */
    @NotNull
    private final String name;

    /**
     * The default value of the generic type that is used, if the type use does
     * not explicitly specify it.
     */
    @Nullable
    private final Type defaultValue;

    /**
     * Get the string representation of the generic type.
     * @return generic type debug information
     */
    @Override
    public String toString() {
        String result = name;
        if (defaultValue != null)
            result += " = " + defaultValue;
        return result;
    }
}
