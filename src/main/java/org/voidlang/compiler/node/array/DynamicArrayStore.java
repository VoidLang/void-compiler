package org.voidlang.compiler.node.array;

import lombok.RequiredArgsConstructor;
import org.voidlang.compiler.node.*;
import org.voidlang.compiler.node.local.Mutable;
import org.voidlang.compiler.node.operator.Accessor;
import org.voidlang.compiler.node.type.core.ScalarType;
import org.voidlang.compiler.node.type.core.Type;
import org.voidlang.compiler.node.value.FunctionContextValue;
import org.voidlang.compiler.node.value.Value;
import org.voidlang.compiler.runtime.Runtime;
import org.voidlang.llvm.element.*;

import java.util.List;

@RequiredArgsConstructor
@NodeInfo(type = NodeType.DYNAMIC_ARRAY_STORE)
public class DynamicArrayStore extends FunctionContextValue {
    private final Accessor accessor;

    private final Accessor index;

    private final Value value;

    /**
     * Generate an LLVM instruction for this node
     *
     * @param generator LLVM instruction generation context
     */
    @Override
    public IRValue generate(Generator generator) {
        IRBuilder builder = generator.getBuilder();
        IRContext context = generator.getContext();

        ScalarType arrayType = (ScalarType) accessor.getValueType();
        int arrayDimensions = arrayType.getArray().getDimensions().size();
        int arrayLength = arrayType.getArray().getDimensions().get(0).getSizeConstant();

        if (!(accessor.getValue() instanceof Mutable))
            throw new IllegalStateException("Cannot store array element for immutable array");

        IRValue irIndex = index.generateAndLoad(generator);
        Runtime.checkIndex(generator, getContext().getFunction(), irIndex, arrayLength);

        IRType irArrayType = arrayType
            .generateType(context)
            .toArrayType(arrayDimensions);
        IRValue arrayPointer = accessor.generate(generator);

        List<IRValue> indices = List.of(IRType.int32(context).constInt(0), irIndex);
        IRValue indexPointer = builder.elementPointer(irArrayType, arrayPointer, indices, "dynamic array store");

        IRValue irValue = value.generate(generator);
        return builder.store(irValue, indexPointer);
    }

    /**
     * Initialize all the child nodes for the overriding node.
     *
     * @param parent parent node of the overriding node
     */
    @Override
    public void preProcess(Node parent) {
        this.parent = parent;
        accessor.preProcess(this);
        index.preProcess(this);
        value.preProcess(this);
    }

    /**
     * Initialize all type declarations for the overriding node.
     *
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessType(Generator generator) {
        accessor.postProcessType(generator);
        index.postProcessType(generator);
        value.postProcessType(generator);
    }

    /**
     * Initialize all class member declarations for the overriding node.
     *
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessMember(Generator generator) {
        accessor.postProcessMember(generator);
        index.postProcessMember(generator);
        value.postProcessMember(generator);
    }

    /**
     * Initialize all type uses for the overriding node.
     *
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessUse(Generator generator) {
        accessor.postProcessUse(generator);
        index.postProcessUse(generator);
        value.postProcessUse(generator);
    }

    /**
     * Get the wrapped type of this value.
     *
     * @return wrapped value type
     */
    @Override
    public Type getValueType() {
        return null;
    }
}
