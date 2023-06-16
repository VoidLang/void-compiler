package org.voidlang.compiler.node.local;

import lombok.Getter;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.compiler.node.type.core.Type;
import org.voidlang.llvm.element.Builder;
import org.voidlang.llvm.element.Value;

@Getter
public class LocalDeclare extends Node {
    private final Type type;

    private final String name;

    public LocalDeclare(Package pkg, Type type, String name) {
        super(NodeType.LOCAL_DECLARE, pkg);
        this.type = type;
        this.name = name;
    }

    /**
     * Generate an LLVM instruction for this node
     * @param builder instruction builder for the current context
     * @return node ir code wrapper
     */
    @Override
    public Value generate(Builder builder) {
        return null;
    }
}
