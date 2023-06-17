package org.voidlang.compiler.node.local;

import lombok.Getter;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.llvm.element.Builder;
import org.voidlang.llvm.element.Value;

@Getter
public class LocalAssign extends Node {
    private final String name;

    private final Node value;

    public LocalAssign(String name, Node value) {
        super(NodeType.LOCAL_ASSIGN);
        this.name = name;
        this.value = value;
    }

    /**
     * Generate an LLVM instruction for this node
     *
     * @param builder instruction builder for the current context
     * @return node ir code wrapper
     */
    @Override
    public Value generate(Builder builder) {
        return null;
    }
}
