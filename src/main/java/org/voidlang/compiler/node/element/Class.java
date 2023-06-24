package org.voidlang.compiler.node.element;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeInfo;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.compiler.node.type.generic.GenericTypeList;
import org.voidlang.llvm.element.IRContext;
import org.voidlang.llvm.element.IRStruct;
import org.voidlang.llvm.element.IRType;
import org.voidlang.llvm.element.IRValue;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Getter
@NodeInfo(type = NodeType.CLASS)
public class Class extends Node {
    private final String name;

    private final GenericTypeList generics;

    private final List<Node> body;

    private IRStruct struct;

    /**
     * Initialize all the child nodes for the overriding node.
     * @param parent parent node of the overriding node
     */
    @Override
    public void preProcess(Node parent) {
        this.parent = parent;
        for (Node node : body)
            node.setParent(this);
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
     * Initialize all type uses for the overriding node.
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessUse(Generator generator) {
        for (Node node : body)
            node.postProcessUse(generator);
    }

    /**
     * Generate an LLVM instruction for this node
     * @param generator LLVM instruction generation context
     */
    @Override
    public IRValue generate(Generator generator) {
        IRContext context = generator.getContext();
        List<IRType> members = new ArrayList<>();
        for (Node node : body) {
            if (node instanceof Field field)
                members.add(field.getType().generateType(context));
            else if (node instanceof MultiField multiField)
                members.add(multiField.getType().generateType(context));
        }
        struct.setMembers(members);
        return null;
    }

    /**
     * Generate an LLVM type for this type element type.
     * @param context LLVM module context
     * @return type ir code wrapper
     */
    public IRType generateType(IRContext context) {
        return struct = IRStruct.define(context, name);
    }
}
