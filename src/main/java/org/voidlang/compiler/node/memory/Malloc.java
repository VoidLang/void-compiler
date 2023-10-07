package org.voidlang.compiler.node.memory;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeInfo;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.compiler.node.local.PointerOwner;
import org.voidlang.compiler.node.type.QualifiedName;
import org.voidlang.compiler.node.type.array.Array;
import org.voidlang.compiler.node.type.core.ScalarType;
import org.voidlang.compiler.node.type.core.Type;
import org.voidlang.compiler.node.type.generic.GenericArgumentList;
import org.voidlang.compiler.node.type.pointer.Referencing;
import org.voidlang.compiler.node.value.Value;
import org.voidlang.llvm.element.*;

@RequiredArgsConstructor
@Getter
@NodeInfo(type = NodeType.MALLOC)
public class Malloc extends Value implements PointerOwner, HeapAllocator {
    private final QualifiedName name;

    private IRType pointerType;
    private IRValue pointer;

    private Type type;
    private Type resolvedType;

    /**
     * Generate an LLVM instruction for this node
     *
     * @param generator LLVM instruction generation context
     */
    @Override
    public IRValue generate(Generator generator) {
        // create an unnamed allocation if the result is not meant to be handled.
        // e.g. if the constructor is called in a method call
        // greet(malloc Person("John"))
        //       ^^^^^^^^ here is an anonymous value of Person, which isn't meant to be mutated
        return allocateHeap(generator, "anonymous malloc");
    }

    @Override
    public IRValue allocateHeap(Generator generator, String name) {
        IRContext context = generator.getContext();
        IRBuilder builder = generator.getBuilder();

        pointerType = type.generateType(context);
        pointer = builder.malloc(pointerType, name);

        // TODO handle field initializers

        return pointer;
    }

    /**
     * Initialize all the child nodes for the overriding node.
     *
     * @param parent parent node of the overriding node
     */
    @Override
    public void preProcess(Node parent) {
        this.parent = parent;
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
        if (name.isPrimitive()) {
            type = new ScalarType(
                Referencing.reference(1),
                name,
                GenericArgumentList.implicit(),
                Array.noArray()
            );
            return;
        }

        type = resolveType(name.getDirect());
        if (type == null)
            throw new IllegalStateException("Unable to fetch type for New: " + name.getDirect());
    }

    @Override
    public IRValue getPointer() {
        return pointer;
    }

    @Override
    public IRType getPointerType() {
        return pointerType;
    }

    @Override
    public Type getValueType() {
        return type;
    }
}
