package org.voidlang.compiler.node.value;

import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.type.core.Type;

public abstract class Value extends Node {
    /**
     * Get the wrapped type of this value.
     * @return wrapped value type
     */
    public abstract Type getValueType();
}
