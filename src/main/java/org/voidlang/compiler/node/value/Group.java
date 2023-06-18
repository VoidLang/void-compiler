package org.voidlang.compiler.node.value;

import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.llvm.element.IRValue;

public class Group extends Node {
    private final Node value;

    public Group(Node value) {
        super(NodeType.GROUP);
        this.value = value;
    }

    /**
     * Generate an LLVM instruction for this node
     *
     * @param generator LLVM instruction generation context
     */
    @Override
    public IRValue generate(Generator generator) {
        return value.generate(generator);
    }
}
