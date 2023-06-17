package org.voidlang.compiler;

import lombok.Getter;
import lombok.Setter;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.compiler.node.element.Method;

@Getter
@Setter
public abstract class Instruction extends Node {
    private Method method;

    public Instruction(NodeType nodeType) {
        super(nodeType);
    }
}
