package org.voidlang.compiler.node.local;

import lombok.Getter;
import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.compiler.node.type.core.Type;
import org.voidlang.llvm.element.IRValue;

@Getter
public class LocalDeclareAssign extends Node {
    private final Type type;

    private final String name;

    private final Node value;

    public LocalDeclareAssign(Type type, String name, Node value) {
        super(NodeType.LOCAL_DECLARE_ASSIGN);
        this.type = type;
        this.name = name;
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
