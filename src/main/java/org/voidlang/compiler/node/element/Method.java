package org.voidlang.compiler.node.element;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeInfo;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.compiler.node.type.core.Type;
import org.voidlang.compiler.node.type.name.Name;
import org.voidlang.compiler.node.type.name.ScalarName;
import org.voidlang.compiler.node.type.named.MethodParameter;
import org.voidlang.llvm.element.*;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Getter
@NodeInfo(type = NodeType.METHOD)
public class Method extends Node {
    private final Type returnType;

    private final String name;

    private final List<MethodParameter> parameters;

    private final List<Node> body;

    private IRFunction function;
    private List<IRType> paramTypes;

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
        paramTypes = parameters
            .stream()
            .map(p -> p.getType().generateType(context))
            .filter(Objects::nonNull)
            .toList();
        IRFunctionType functionType = IRFunctionType.create(returnType, paramTypes);

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

    public boolean checkTypes(List<Type> types) {
        if (types.size() != parameters.size())
            return false;
        for (int i = 0; i < types.size(); i++) {
            if (types.get(i).equals(parameters.get(i).getType()))
                return false;
        }
        return true;
    }

    public IRFunction getFunction() {
        return function;
    }

    @Override
    public Node resolveName(String name) {
        for (int i = 0; i < parameters.size(); i++) {
            MethodParameter parameter = parameters.get(i);
            Name paramName = parameter.getName();
            if (paramName.isScalar()) {
                String value = ((ScalarName) paramName).getValue();
                if (!value.equals(name))
                    continue;
                return new ParameterIndexer(i);
            }
        }
        return super.resolveName(name);
    }

    @RequiredArgsConstructor
    @NodeInfo(type = NodeType.PARAMETER_INDEXER)
    private class ParameterIndexer extends Node {
        private final int index;

        /**
         * Generate an LLVM instruction for this node
         * @param generator LLVM instruction generation context
         */
        @Override
        public IRValue generate(Generator generator) {
            return function.getParameter(index);
        }
    }
}
