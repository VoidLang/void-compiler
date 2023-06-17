package org.voidlang.compiler.node.local;

import lombok.Getter;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.compiler.node.type.name.CompoundName;
import org.voidlang.llvm.element.Builder;
import org.voidlang.llvm.element.Value;

@Getter
public class LocalDeclareDestructureTuple extends Node {
    private final CompoundName name;

    private final Node value;

    public LocalDeclareDestructureTuple(CompoundName name, Node value) {
        super(NodeType.LOCAL_DECLARE_DESTRUCTURE_TUPLE);
        this.name = name;
        this.value = value;
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
