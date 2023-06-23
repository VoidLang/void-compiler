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
     * Generate an LLVM instruction for this node
     * @param generator LLVM instruction generation context
     */
    @Override
    public IRValue generate(Generator generator) {
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
