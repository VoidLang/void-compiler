package org.voidlang.compiler.node.operator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeInfo;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.compiler.node.type.QualifiedName;
import org.voidlang.llvm.element.IRValue;

@RequiredArgsConstructor
@Getter
@NodeInfo(type = NodeType.ACCESSOR)
public class Accessor extends Node {
    private final QualifiedName name;

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
