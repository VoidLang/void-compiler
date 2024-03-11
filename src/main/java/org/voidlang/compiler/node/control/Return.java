package org.voidlang.compiler.node.control;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.voidlang.compiler.node.*;
import org.voidlang.compiler.node.method.Instruction;
import org.voidlang.compiler.node.type.core.Type;
import org.voidlang.compiler.node.type.named.NamedScalarType;
import org.voidlang.compiler.node.value.Tuple;
import org.voidlang.compiler.node.value.Value;
import org.voidlang.llvm.element.*;

@RequiredArgsConstructor
@Getter
@NodeInfo(type = NodeType.RETURN)
public class Return extends Instruction {
    @Nullable
    private final Value value;

    /**
     * Generate an LLVM instruction for this node
     * @param generator LLVM instruction generation context
     */
    @Override
    public IRValue generate(Generator generator) {
        IRBuilder builder = generator.getBuilder();
        IRContext context = generator.getContext();

        // simply return a void if no value was specified
        // TODO check if the method actually returns void
        if (value == null)
            return builder.returnVoid();

        // resolve the type of the value to be returned and the return type of the function
        Type valueType = value.getValueType();
        Type returnType = getContext().getReturnType();

        // extract the type of the value
        if (valueType instanceof NamedScalarType namedValueType)
            valueType = namedValueType.getScalarType();

        // extract the type of the return type
        if (returnType instanceof NamedScalarType namedReturnType)
            returnType = namedReturnType.getScalarType();

        // handle tuple allocation and return
        if (value instanceof Tuple tuple) {
            // retrieve the tuple type from the method context
            Type structType = getContext().getReturnType();
            IRType struct = structType.generateType(context);
            // allocate the tuple on the stack
            IRValue tuplePtr = tuple.generateTuple(generator, (IRStruct) struct);
            // load the value of the tuple
            IRValue value = builder.load(struct, tuplePtr, "tuple value");
            // let the current block to be terminated, and the value be returned
            return builder.returnValue(value);
        }

        // check if the value does not match the method return type
        if (!valueType.equals(returnType))
            throw new IllegalStateException(
                "Cannot return " + valueType + "(" + valueType.getClass().getSimpleName() + ") in a " +
                returnType + "(" + returnType.getClass().getSimpleName() + ") function"
            );

        // generate the code to the value to be returned
        IRValue value = getValue().generateAndLoad(generator);
        // let the current block to be terminated, and the value be returned
        return builder.returnValue(value);
    }

    /**
     * Initialize all the child nodes for the overriding node.
     * @param parent parent node of the overriding node
     */
    @Override
    public void preProcess(Node parent) {
        if (value != null)
            value.preProcess(parent);
    }

    /**
     * Initialize all type declarations for the overriding node.
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessType(Generator generator) {
        if (value != null)
            value.postProcessType(generator);
    }

    /**
     * Initialize all class member declarations for the overriding node.
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessMember(Generator generator) {
        if (value != null)
            value.postProcessMember(generator);
    }

    /**
     * Initialize all type uses for the overriding node.
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessUse(Generator generator) {
        if (value != null)
            value.postProcessUse(generator);
    }

    @Override
    public String toString() {
        return "return " + value;
    }
}
