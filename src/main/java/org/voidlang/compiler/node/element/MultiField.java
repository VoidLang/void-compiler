package org.voidlang.compiler.node.element;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeInfo;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.compiler.node.type.core.Type;
import org.voidlang.llvm.element.IRValue;

import java.util.Map;

@RequiredArgsConstructor
@Getter
@NodeInfo(type = NodeType.MULTI_FIELD)
public class MultiField extends Node {
    private final Type type;

    private final Map<String, @Nullable Node> values;

    /**
     * Generate an LLVM instruction for this node
     * @param generator LLVM instruction generation context
     */
    @Override
    public IRValue generate(Generator generator) {
        return null;
    }

    /**
     * Initialize all the child nodes for the overriding node.
     * @param parent parent node of the overriding node
     */
    @Override
    public void preProcess(Node parent) {
        this.parent = parent;
        for (Node node : values.values()) {
            if (node == null)
                continue;
            node.preProcess(this);
        }
    }

    /**
     * Initialize all type declarations for the overriding node.
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessType(Generator generator) {
        for (Node node : values.values()) {
            if (node == null)
                continue;
            node.postProcessType(generator);
        }
    }

    /**
     * Initialize all class member declarations for the overriding node.
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessMember(Generator generator) {
        for (Node node : values.values()) {
            if (node == null)
                continue;
            node.postProcessMember(generator);
        }
    }

    /**
     * Initialize all type uses for the overriding node.
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessUse(Generator generator) {
        for (Node node : values.values()) {
            if (node == null)
                continue;
            node.postProcessUse(generator);
        }
    }
}
