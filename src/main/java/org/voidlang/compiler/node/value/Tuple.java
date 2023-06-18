package org.voidlang.compiler.node.value;

import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.llvm.element.IRValue;

import java.util.List;

public class Tuple extends Node {
    private final List<Node> members;

    public Tuple(List<Node> members) {
        super(NodeType.TUPLE);
        this.members = members;
    }

    /**
     * Generate an LLVM instruction for this node
     *
     * @param generator LLVM instruction generation context
     */
    @Override
    public IRValue generate(Generator generator) {
        return null;
    }
}
