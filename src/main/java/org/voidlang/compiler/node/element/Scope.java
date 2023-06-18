package org.voidlang.compiler.node.element;

import lombok.RequiredArgsConstructor;
import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.Node;
import org.voidlang.llvm.element.IRValue;

import java.util.List;

@RequiredArgsConstructor
public abstract class Scope extends Node {
    private final List<Node> body;

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
