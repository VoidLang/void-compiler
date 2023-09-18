package org.voidlang.compiler.node.type.pointer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeInfo;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.compiler.node.local.Mutable;
import org.voidlang.compiler.node.local.PointerOwner;
import org.voidlang.compiler.node.type.QualifiedName;
import org.voidlang.compiler.node.type.core.ScalarType;
import org.voidlang.compiler.node.type.core.Type;
import org.voidlang.compiler.node.value.Value;
import org.voidlang.llvm.element.IRType;
import org.voidlang.llvm.element.IRValue;

@RequiredArgsConstructor
@Getter
@NodeInfo(type = NodeType.REFERENCED_ACCESSOR)
public class ReferencedAccessor extends Value implements PointerOwner, Mutable {
    @NotNull
    private final QualifiedName name;

    private PointerOwner value;

    private Type valueType;

    /**
     * Generate an LLVM instruction for this node
     *
     * @param generator LLVM instruction generation context
     */
    @Override
    public IRValue generate(Generator generator) {
        // TODO handle fields
        return value.getPointer();
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
            throw new IllegalStateException("Unable to fetch ref value: " + name.getDirect());
        if (!(value instanceof PointerOwner owner))
            throw new IllegalStateException("Referencing a non-pointer-owner " + name);
        this.value = owner;

        Type type = owner.getValueType();
        if (!(type instanceof ScalarType scalar))
            throw new IllegalStateException("Referencing a non-scalar-type value: " + type);

        valueType = new ScalarType(Referencing.reference(scalar.getReferencing().getDimensions() + 1),
            scalar.getName(), scalar.getGenerics(), scalar.getArray());
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
        // TODO handle field access
        return valueType;
    }
}
