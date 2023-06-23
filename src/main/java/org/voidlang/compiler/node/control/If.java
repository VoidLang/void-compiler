package org.voidlang.compiler.node.control;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeInfo;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.llvm.element.IRValue;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Getter
@NodeInfo(type = NodeType.IF)
public class If extends Node {
    private final Node condition;

    private final List<Node> body;

    private final List<ElseIf> elseIfs = new ArrayList<>();

    @Setter
    private Else elseCase;

    /**
     * Generate an LLVM instruction for this node
     * @param generator LLVM instruction generation context
     */
    @Override
    public IRValue generate(Generator generator) {
        return null;
    }
}
