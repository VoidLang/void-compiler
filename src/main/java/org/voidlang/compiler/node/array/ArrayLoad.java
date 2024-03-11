package org.voidlang.compiler.node.array;

import lombok.RequiredArgsConstructor;
import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeInfo;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.compiler.node.operator.Accessor;
import org.voidlang.compiler.node.type.array.Array;
import org.voidlang.compiler.node.type.array.Dimension;
import org.voidlang.compiler.node.type.core.ScalarType;
import org.voidlang.compiler.node.type.core.Type;
import org.voidlang.compiler.node.value.Value;
import org.voidlang.llvm.element.IRBuilder;
import org.voidlang.llvm.element.IRContext;
import org.voidlang.llvm.element.IRType;
import org.voidlang.llvm.element.IRValue;

import java.util.ArrayList;
import java.util.List;

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

        ScalarType arrayType = (ScalarType) accessor.getValueType();
        int arraySize = arrayType.getArray().getDimensions().size();

        IRType irArrayType = arrayType
            .generateType(generator.getContext())
            .toArrayType(arraySize);
        IRValue arrayPointer = accessor.generate(generator);

        IRValue indexPointer = builder.structMemberPointer(irArrayType, arrayPointer, index, "array load[" + index + "]");

        IRType indexType = elementType.generateType(context);
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

        // remove the first dimension from the array type
        Array newArray;
        if (arrayType.getArray().getDimensions().size() == 1)
            newArray = Array.noArray();
        else {
            List<Dimension> newDimensions = new ArrayList<>(arrayType.getArray().getDimensions());
            newDimensions.remove(0);
            newArray = Array.of(newDimensions);
        }

        elementType = new ScalarType(
            arrayType.getReferencing(),
            arrayType.getName(),
            arrayType.getGenerics(),
            newArray
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
