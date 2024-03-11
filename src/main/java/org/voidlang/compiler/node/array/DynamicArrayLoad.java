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
import org.voidlang.compiler.node.value.FunctionContextValue;
import org.voidlang.compiler.runtime.Runtime;
import org.voidlang.llvm.element.IRBuilder;
import org.voidlang.llvm.element.IRContext;
import org.voidlang.llvm.element.IRType;
import org.voidlang.llvm.element.IRValue;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@NodeInfo(type = NodeType.DYNAMIC_ARRAY_LOAD)
public class DynamicArrayLoad extends FunctionContextValue {
    private final Accessor accessor;

    private final Accessor index;

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
        int arrayDimensions = arrayType.getArray().getDimensions().size();
        int arrayLength = arrayType.getArray().getDimensions().get(0).getSizeConstant();

        IRValue irIndex = index.generateAndLoad(generator);
        Runtime.checkIndex(generator, resolveMethodScope().getFunction(), irIndex, arrayLength);

        IRType irArrayType = arrayType
            .generateType(context)
            .toArrayType(arrayDimensions);
        IRValue arrayPointer = accessor.generate(generator);

        List<IRValue> indices = List.of(
            IRType.int32(context).constInt(0),
            index.generateAndLoad(generator)
        );
        IRValue indexPointer = builder.elementPointer(irArrayType, arrayPointer, indices, "dynamic array load");

        IRType indexType = elementType.generateType(context);
        return builder.load(indexType, indexPointer, "dynamic array value");
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
