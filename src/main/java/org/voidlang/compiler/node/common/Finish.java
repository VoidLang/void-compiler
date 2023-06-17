package org.voidlang.compiler.node.common;

import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.llvm.element.Builder;
import org.voidlang.llvm.element.Value;

/**
 * Represents a node that indicates that the parsing of the file has been ended.
 */
public class Finish extends Node {
    /**
     * Initialize the node.
     */
    public Finish() {
        super(NodeType.FINISH);
    }

    /**
     * Generate an LLVM instruction for this node
     *
     * @return node ir code wrapper
     */
    @Override
    public Value generate(Builder builder) {
        throw new IllegalStateException("Cannot generate IR code for " + Finish.class);
    }
}
