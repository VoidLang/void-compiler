package org.voidlang.compiler.node.array;

import lombok.RequiredArgsConstructor;
import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeInfo;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.compiler.node.operator.Accessor;
import org.voidlang.compiler.node.type.array.Array;
import org.voidlang.compiler.node.type.core.ScalarType;
import org.voidlang.compiler.node.type.core.Type;
import org.voidlang.compiler.node.value.Value;
import org.voidlang.llvm.element.IRBuilder;
import org.voidlang.llvm.element.IRContext;
import org.voidlang.llvm.element.IRType;
import org.voidlang.llvm.element.IRValue;

@RequiredArgsConstructor
@NodeInfo(type = NodeType.ARRAY_LOAD)
public class ArrayLoad extends Value {
    private final Accessor accessor;

    private final int index;

    private ScalarType elementType;

    /**
     * Generate an LLVM instruction for this node
     *
     * @param generator LLVM instruction generation context
     */
    @Override
    public IRValue generate(Generator generator) {
        IRBuilder builder = generator.getBuilder();
        IRContext context = generator.getContext();

        ScalarType elementType = (ScalarType) accessor.getValueType();

        int arraySize = elementType.getArray().getDimensions().size();

        IRType arrayType = elementType
            .generateType(generator.getContext())
            .toArrayType(arraySize);
        IRValue arrayPointer = accessor.generate(generator);

        IRValue indexPointer = builder.structMemberPointer(arrayType, arrayPointer, index, "array load[" + index + "]");

        IRType indexType = this.elementType.generateType(context);
        return builder.load(indexType, indexPointer, "array value[" + index + "]");
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
    }

    /**
     * Initialize all type declarations for the overriding node.
     *
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessType(Generator generator) {
        accessor.postProcessType(generator);
    }

    /**
     * Initialize all class member declarations for the overriding node.
     *
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessMember(Generator generator) {
        accessor.postProcessMember(generator);
    }

    /**
     * Initialize all type uses for the overriding node.
     *
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessUse(Generator generator) {
        accessor.postProcessUse(generator);

        ScalarType arrayType = (ScalarType) accessor.getValueType();

        int dimensions = arrayType
            .getArray()
            .getDimensions()
            .size() - 1;

        elementType = new ScalarType(
            arrayType.getReferencing(),
            arrayType.getName(),
            arrayType.getGenerics(),
            dimensions == 0 ? Array.noArray() : Array.explicit(dimensions - 1)
        );
    }

    /**
     * Get the wrapped type of this value.
     *
     * @return wrapped value type
     */
    @Override
    public Type getValueType() {
        return elementType;
    }
}
