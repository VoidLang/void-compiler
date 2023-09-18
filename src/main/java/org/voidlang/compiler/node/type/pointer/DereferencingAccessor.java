package org.voidlang.compiler.node.type.pointer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
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
@NodeInfo(type = NodeType.DEREFERENCED_ACCESSOR)
public class DereferencingAccessor extends Value implements PointerOwner {
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
        IRBuilder builder = generator.getBuilder();
        IRContext context = builder.getContext();
        return builder.load(valueType.generateType(context), value.getPointer(),"deref");
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
            throw new IllegalStateException("Referencing a non-scalar-type: " + type);

        Referencing referencing = scalar.getReferencing();
        if (referencing.getType() != ReferencingType.REFERENCE)
            throw new IllegalStateException("Cannot dereference a non-reference type: " + type);

        Referencing newReferencing = referencing.getDimensions() > 1
            ? Referencing.reference(referencing.getDimensions() - 1)
            : Referencing.none(); // TODO should it be mutable instead?

        valueType = new ScalarType(newReferencing,
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
