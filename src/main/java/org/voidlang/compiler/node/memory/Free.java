package org.voidlang.compiler.node.memory;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeInfo;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.compiler.node.local.PointerOwner;
import org.voidlang.compiler.node.type.QualifiedName;
import org.voidlang.compiler.node.type.core.ScalarType;
import org.voidlang.compiler.node.type.core.Type;
import org.voidlang.compiler.node.value.Value;
import org.voidlang.llvm.element.IRBuilder;
import org.voidlang.llvm.element.IRContext;
import org.voidlang.llvm.element.IRType;
import org.voidlang.llvm.element.IRValue;

@RequiredArgsConstructor
@Getter
@NodeInfo(type = NodeType.FREE)
public class Free extends Value implements PointerOwner {
    private final QualifiedName name;

    private PointerOwner value;

    /**
     * Generate an LLVM instruction for this node
     *
     * @param generator LLVM instruction generation context
     */
    @Override
    public IRValue generate(Generator generator) {
        IRBuilder builder = generator.getBuilder();

        return builder.free(value.getPointer());
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
        Value value = resolveName(name.getDirect());
        if (value == null)
            throw new IllegalStateException("Unable to fetch free value: " + name.getDirect());
        if (!(value instanceof PointerOwner owner))
            throw new IllegalStateException("Freeing a non-pointer-owner " + name);
        this.value = owner;

        Type type = owner.getValueType();
        if (!(type instanceof ScalarType scalar))
            throw new IllegalStateException("Freeing a non-scalar-type: " + type);
    }

    @Override
    public IRValue getPointer() {
        return value.getPointer();
    }

    @Override
    public IRType getPointerType() {
        return value.getPointerType();
    }

    /**
     * Get the wrapped type of this value.
     *
     * @return wrapped value type
     */
    @Override
    public Type getValueType() {
        return value.getValueType();
    }
}
