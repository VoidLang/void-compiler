package org.voidlang.compiler.node.operator;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeInfo;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.compiler.node.control.Element;
import org.voidlang.compiler.node.element.Field;
import org.voidlang.compiler.node.local.Loadable;
import org.voidlang.compiler.node.local.PointerOwner;
import org.voidlang.compiler.node.type.QualifiedName;
import org.voidlang.compiler.node.type.core.Type;
import org.voidlang.compiler.node.value.Value;
import org.voidlang.llvm.element.*;

@RequiredArgsConstructor
@Getter
@NodeInfo(type = NodeType.ACCESSOR)
public class Accessor extends Value implements Loadable {
    private final QualifiedName name;

    private Value value;

    /**
     * Initialize all the child nodes for the overriding node.
     * @param parent parent node of the overriding node
     */
    @Override
    public void preProcess(Node parent) {
        this.parent = parent;
    }

    /**
     * Initialize all type declarations for the overriding node.
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessType(Generator generator) {
    }

    /**
     * Initialize all class member declarations for the overriding node.
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessMember(Generator generator) {
    }

    /**
     * Initialize all type uses for the overriding node.
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessUse(Generator generator) {
        value = resolveName(name.getDirect());
        if (value == null)
            throw new IllegalStateException("Unable to fetch New value: " + name.getDirect());
        // postProcessUse is disabled here, as it would create an infinite loop
    }

    /**
     * Generate an LLVM instruction for this node
     * @param generator LLVM instruction generation context
     */
    @Override
    public IRValue generate(Generator generator) {
        if (value instanceof Loadable loadable && !getName().isFieldAccess())
            // TODO probably should use Loadable#load() here as well
            return value.generateAndLoad(generator);

        IRContext context = generator.getContext();
        IRBuilder builder = generator.getBuilder();

        if (name.isFieldAccess()) {
            PointerOwner owner = (PointerOwner) value;
            IRValue instance = owner.getPointer();

            Element element = (Element) getValueType();
            IRStruct rootType = (IRStruct) element.generateType(context);
            Field field = element.resolveField(name.getFieldName());

            return builder.structMemberPointer(rootType, instance, field.getFieldIndex(), field.getName());
        }

        return value.generate(generator);
    }

    @Override
    public IRValue generateAndLoad(Generator generator) {
        if (value instanceof Loadable loadable && !getName().isFieldAccess())
            return loadable.load(generator);

        IRContext context = generator.getContext();
        IRBuilder builder = generator.getBuilder();

        if (name.isFieldAccess()) {
            PointerOwner owner = (PointerOwner) value;
            IRValue instance = owner.getPointer();

            Element element = (Element) getValueType();
            IRStruct rootType = (IRStruct) element.generateType(context);
            Field field = element.resolveField(name.getFieldName());

            IRValue pointer = builder.structMemberPointer(rootType, instance, field.getFieldIndex(), field.getName());
            IRType fieldType = field.getType().generateType(context);

            return builder.load(fieldType, pointer, "");
        }

        return value.generate(generator);
    }

    @Override
    public IRValue load(Generator generator) {
        return generateAndLoad(generator);
    }

    /**
     * Get the wrapped type of this value.
     * @return wrapped value type
     */
    @Override
    public Type getValueType() {
        return value.getValueType();
    }
}
