package org.voidlang.compiler.node.element;

import lombok.Getter;
import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.compiler.node.type.core.Type;
import org.voidlang.compiler.node.type.named.MethodParameter;
import org.voidlang.llvm.element.*;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Method extends Node {
    private final Type returnType;

    private final String name;

    private final List<MethodParameter> parameters;

    private final List<Node> body;

    public Method(Type returnType, String name, List<MethodParameter> parameters, List<Node> body) {
        super(NodeType.METHOD);
        this.returnType = returnType;
        this.name = name;
        this.parameters = parameters;
        this.body = body;
    }

    /**
     * Generate an LLVM instruction for this node
     * @param generator LLVM instruction generation context
     */
    @Override
    public IRValue generate(Generator generator) {
        IRContext context = generator.getContext();
        IRModule module = generator.getModule();

        IRType returnType = getReturnType().generateType(context);
        IRFunctionType functionType = IRFunctionType.create(returnType, new ArrayList<>());

        return IRFunction.create(module, name, functionType);
    }
}
