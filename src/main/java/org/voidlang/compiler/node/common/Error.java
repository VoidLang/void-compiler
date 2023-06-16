package org.voidlang.compiler.node.common;

import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.llvm.element.Builder;
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
     * Generate an LLVM instruction for this node
     * @return node ir code wrapper
     */
    @Override
    public Value generate(Builder builder) {
        throw new IllegalStateException("Cannot generate IR code for " + Error.class);
    }
}
