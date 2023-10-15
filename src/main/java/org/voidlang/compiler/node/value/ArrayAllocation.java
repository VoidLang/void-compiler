package org.voidlang.compiler.node.value;

import lombok.RequiredArgsConstructor;
import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeInfo;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.compiler.node.type.QualifiedName;
import org.voidlang.compiler.node.type.array.Array;
import org.voidlang.compiler.node.type.core.ScalarType;
import org.voidlang.compiler.node.type.core.Type;
import org.voidlang.compiler.node.type.generic.GenericArgumentList;
import org.voidlang.compiler.node.type.pointer.Referencing;
import org.voidlang.llvm.element.IRBuilder;
import org.voidlang.llvm.element.IRContext;
import org.voidlang.llvm.element.IRType;
import org.voidlang.llvm.element.IRValue;

import java.util.List;

@RequiredArgsConstructor
@NodeInfo(type = NodeType.ARRAY)
public class ArrayAllocation extends Value {
    private final List<Value> values;

    private IRType arrayType;
    private ScalarType elementType;
    private Type valueType;

    /**
     * Generate an LLVM instruction for this node
     *
     * @param generator LLVM instruction generation context
     */
    @Override
    public IRValue generate(Generator generator) {
        IRContext context = generator.getContext();
        IRBuilder builder = generator.getBuilder();

        IRType irElementType = elementType.generateType(context);
        arrayType = irElementType.toArrayType(values.size());

        return builder.alloc(arrayType, "let array");
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

    /**
     * Initialize all type declarations for the overriding node.
     *
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessType(Generator generator) {

    }

    /**
     * Initialize all class member declarations for the overriding node.
     *
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessMember(Generator generator) {

    }

    /**
     * Initialize all type uses for the overriding node.
     *
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessUse(Generator generator) {

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
