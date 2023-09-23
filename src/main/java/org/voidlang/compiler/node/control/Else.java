package org.voidlang.compiler.node.control;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeInfo;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.compiler.node.local.ImmutableLocalDeclareAssign;
import org.voidlang.compiler.node.local.MutableLocalDeclareAssign;
import org.voidlang.compiler.node.value.Value;
import org.voidlang.llvm.element.IRValue;

import java.util.List;

@RequiredArgsConstructor
@Getter
@NodeInfo(type = NodeType.ELSE)
public class Else extends Node {
    private final List<Node> body;

    /**
     * Initialize all the child nodes for the overriding node.
     * @param parent parent node of the overriding node
     */
    @Override
    public void preProcess(Node parent) {
        this.parent = parent;
        for (Node node : body)
            node.preProcess(this);
    }

    /**
     * Initialize all type declarations for the overriding node.
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessType(Generator generator) {
        for (Node node : body)
            node.postProcessType(generator);
    }

    /**
     * Initialize all class member declarations for the overriding node.
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessMember(Generator generator) {
        for (Node node : body)
            node.postProcessMember(generator);
    }

    /**
     * Initialize all type uses for the overriding node.
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessUse(Generator generator) {
        for (Node node : body)
            node.postProcessUse(generator);
    }

    /**
     * Resolve a node from this node context by its name. If the name is unresolved locally,
     * the parent element tries to resolve it.
     * @param name target node name
     * @return resolved node or null if it was not found
     */
    @Override
    public @Nullable Value resolveName(String name) {
        // resolve local variables in the method body
        for (Node node : body) {
            if (node instanceof ImmutableLocalDeclareAssign local && local.getName().equals(name))
                return local;
            else if (node instanceof MutableLocalDeclareAssign local && local.getName().equals(name))
                return local;
        }

        // let the parent nodes recursively resolve the name
        return super.resolveName(name);
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
