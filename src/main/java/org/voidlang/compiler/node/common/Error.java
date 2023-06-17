package org.voidlang.compiler.node.common;

import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.llvm.element.IRValue;

/**
 * Represents a node that holds the information of a compiling error
 * that occurred whilst parsing tokens to nodes.
 */
public class Error extends Node {
    /**
     * Initialize the node.
     */
    public Error() {
        super(NodeType.ERROR);
    }

    /**
     * Generate an LLVM instruction for this node
     * @param generator LLVM instruction generation context
     */
    @Override
    public IRValue generate(Generator generator) {
        return null;
    }
}
