package org.voidlang.compiler.node.method;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.voidlang.compiler.node.type.core.TypeEntry;
import org.voidlang.compiler.node.type.name.NameEntry;
import org.voidlang.compiler.node.type.core.Type;
import org.voidlang.compiler.node.type.core.TypeGroup;

/**
 * Represents an information holder of a method parameter.
 * <p>Examples:</p>
 * <pre> {@code
 *     void foo(int x, float y)
 *     void bar((int, int) point)
 *     void baz(Point (x, y))
 *     void qux(Entity { health, level })
 *     void abc(int[] [x, y, z])
 * } </pre>
 * The {@link TypeEntry} allows the parameter type to be a single scalar type {@link Type} or a compound
 * type {@link TypeGroup} for grouping multiple types together.
 * The {@link NameEntry} allows the parameter to use deconstruction on compound types:
 */
@AllArgsConstructor
@Getter
public class Parameter {
    /**
     * The type entry of the method parameter. Can be either a scalar or a compound type.
     */
    private final TypeEntry type;

    /**
     * Indicate, whether the parameter is variadic. If this is true, the <code>name</code>
     * must be a normal parameter name.
     */
    private final boolean variadic;

    /**
     * The name entry of the method parameter. Can be either a normal parameter name or a
     * compound-type object deconstruction.
     */
    private final NameEntry name;
}
