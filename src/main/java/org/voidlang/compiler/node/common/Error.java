package org.voidlang.compiler.node.common;

import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeInfo;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.compiler.node.type.core.Type;
import org.voidlang.compiler.node.value.Value;
import org.voidlang.llvm.element.IRValue;

/**
 * Represents a node that holds the information of a compiling error
 * that occurred whilst parsing tokens to nodes.
 */
@NodeInfo(type = NodeType.ERROR)
public class Error extends Value {
    public Error() {
        System.exit(-1);
    }

    /**
     * Generate an LLVM instruction for this node
     * @param generator LLVM instruction generation context
     */
    @Override
    public IRValue generate(Generator generator) {
        return null;
    }

    /**
     * Get the wrapped type of this value.
     * @return wrapped value type
     */
    @Override
    public Type getValueType() {
        return null;
    }
}
