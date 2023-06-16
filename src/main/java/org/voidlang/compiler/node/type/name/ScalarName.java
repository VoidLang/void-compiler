package org.voidlang.compiler.node.type.name;

import dev.inventex.octa.console.ConsoleFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents a parameter name in the Void syntax that consists of a single value.
 * <p>Example:</p>
 * <pre> {@code
 *     void foo(int x)
 * } </pre>
 * Here {@code x} is the name value.
 */
@AllArgsConstructor
@Getter
public class ScalarName implements Name {
    /**
     * The value of the scalar name.
     */
    private final String value;

    /**
     * Get the string representation of the scalar type.
     * @return scalar type debug information
     */
    @Override
    public String toString() {
        return ConsoleFormat.WHITE + value;
    }
}
