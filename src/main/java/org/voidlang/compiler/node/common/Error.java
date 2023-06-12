package org.voidlang.compiler.node.common;

import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.llvm.element.Value;

/**
 * Represents a node that holds the information of a compiling error
 * that occurred whilst parsing tokens to nodes.
 */
public class Error extends Node {
    /**
     * Initialize the node.
     */
    public Error() {
        super(NodeType.ERROR, null);
    }

    /**
     * Print the string representation of this node.
     */
    @Override
    public void debug() {
        System.out.println("Error");
    }

    /**
     * Generate an LLVM instruction for this node
     * @return node ir code wrapper
     */
    @Override
    public Value generate() {
        throw new IllegalStateException("Cannot generate IR code for " + Error.class);
    }
}
