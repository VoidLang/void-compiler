package org.voidlang.compiler.node.common;

import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.llvm.element.IRValue;

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
     * @param generator LLVM instruction generation context
     */
    @Override
    public IRValue generate(Generator generator) {
        return null;
    }
}
