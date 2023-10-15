package org.voidlang.compiler.node.value;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeInfo;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.compiler.node.type.QualifiedName;
import org.voidlang.compiler.node.type.array.Array;
import org.voidlang.compiler.node.type.core.ScalarType;
import org.voidlang.compiler.node.type.core.Type;
import org.voidlang.llvm.element.IRBuilder;
import org.voidlang.llvm.element.IRContext;
import org.voidlang.llvm.element.IRType;
import org.voidlang.llvm.element.IRValue;

import java.util.List;

@RequiredArgsConstructor
@NodeInfo(type = NodeType.ARRAY)
public class ArrayAllocation extends Value {
    private final List<Value> values;

    private ScalarType elementType;
    private Type valueType;

    private IRType arrayType;
    @Getter
    private IRValue arrayPointer;

    /**
     * Generate an LLVM instruction for this node
     *
     * @param generator LLVM instruction generation context
     */
    @Override
    public IRValue generate(Generator generator) {
        IRContext context = generator.getContext();
        IRBuilder builder = generator.getBuilder();

        // generate the type for the array of a fixed size
        arrayType = elementType
            .generateType(context)
            .toArrayType(values.size());

        // allocate the fixed size array on the stack
        arrayPointer = builder.alloc(arrayType, "let array");

        // initialize the array elements
        for (int i = 0; i < values.size(); i++) {
            Value value = values.get(i);
            IRValue valuePointer = value.generateAndLoad(generator);
            IRValue indexPointer = getIndexPointer(generator, i);
            builder.store(valuePointer, indexPointer);
        }

        return arrayPointer;
    }

    /**
     * Initialize all the child nodes for the overriding node.
     *
     * @param parent parent node of the overriding node
     */
    @Override
    public void preProcess(Node parent) {
        this.parent = parent;
        for (Value value : values)
            value.preProcess(this);

        if (values.isEmpty())
            throw new IllegalStateException("Cannot initialize empty array. Unable to infer type.");

        elementType = (ScalarType) values
            .get(0)
            .getValueType();

        for (int i = 1; i < values.size(); i++) {
            Type currentType = values.get(i).getValueType();
            if (!currentType.equals(elementType))
                throw new IllegalStateException(
                    "Cannot initialize array with different types. First element type is " + elementType +
                    ", but " + (i + 1) + ". element type is " + currentType
                );
        }

        int dimensions = elementType
            .getArray()
            .getDimensions()
            .size() + 1;

        valueType = new ScalarType(
            elementType.getReferencing(),
            elementType.getName(),
            elementType.getGenerics(),
            Array.explicit(dimensions)
        );
    }

    private IRValue getIndexPointer(Generator generator, int index) {
        IRBuilder builder = generator.getBuilder();

        return builder.structMemberPointer(arrayType, arrayPointer, index, "array[" + index + "]");
    }

    /**
     * Initialize all type declarations for the overriding node.
     *
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessType(Generator generator) {
        for (Value value : values)
            value.postProcessType(generator);
    }

    /**
     * Initialize all class member declarations for the overriding node.
     *
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessMember(Generator generator) {
        for (Value value : values)
            value.postProcessMember(generator);
    }

    /**
     * Initialize all type uses for the overriding node.
     *
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessUse(Generator generator) {
        for (Value value : values)
            value.postProcessUse(generator);
    }

    /**
     * Get the wrapped type of this value.
     *
     * @return wrapped value type
     */
    @Override
    public Type getValueType() {
        return valueType;
    }
}
