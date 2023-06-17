package org.voidlang.compiler.node.operator;

import lombok.Getter;
import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.compiler.token.Token;
import org.voidlang.llvm.element.IRValue;

@Getter
public class Value extends Node {
    private final Token value;

    public Value(Token value) {
        super(NodeType.VALUE);
        this.value = value;
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
