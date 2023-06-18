package org.voidlang.compiler.node.element;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeInfo;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.compiler.node.type.core.Type;
import org.voidlang.compiler.node.type.named.MethodParameter;
import org.voidlang.llvm.element.*;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Getter
@NodeInfo(type = NodeType.METHOD)
public class Method extends Node {
    private final Type returnType;

    private final String name;

    private final List<MethodParameter> parameters;

    private final List<Node> body;

    private IRFunction function;

    /**
     * Generate an LLVM instruction for this node
     * @param generator LLVM instruction generation context
     */
    @Override
    public IRValue generate(Generator generator) {
        // extract the context from the generator
        IRContext context = generator.getContext();
        IRModule module = generator.getModule();
        IRBuilder builder = generator.getBuilder();

        // create the signature of the LLVM function
        IRType returnType = getReturnType().generateType(context);
        IRFunctionType functionType = IRFunctionType.create(returnType, new ArrayList<>());
        // create the LLVM function for the target context
        function = IRFunction.create(module, name, functionType);

        // create an entry block for the function
        IRBlock block = IRBlock.create(context, function, "entry");
        builder.positionAtEnd(block);

        // generate the LLVM instructions for the body of the function
        for (Node instruction : body)
            instruction.generate(generator);

        return function;
    }

    public IRFunction getFunction() {
        return function;
    }
}
