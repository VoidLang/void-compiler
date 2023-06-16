package org.voidlang.compiler.node.operand;

import lombok.Getter;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.compiler.token.Token;
import org.voidlang.llvm.element.Builder;

@Getter
public class Value extends Node {
    private final Token value;

    public Value(Package pkg, Token value) {
        super(NodeType.VALUE, pkg);
        this.value = value;
    }

    /**
     * Generate an LLVM instruction for this node
     *
     * @param builder instruction builder for the current context
     * @return node ir code wrapper
     */
    @Override
    public org.voidlang.llvm.element.Value generate(Builder builder) {
        return null;
    }
}
