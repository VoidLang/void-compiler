package org.voidlang.compiler.node.operator;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeInfo;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.compiler.node.control.Element;
import org.voidlang.compiler.node.element.Field;
import org.voidlang.compiler.node.local.Loadable;
import org.voidlang.compiler.node.local.PassedByReference;
import org.voidlang.compiler.node.local.PointerOwner;
import org.voidlang.compiler.node.type.QualifiedName;
import org.voidlang.compiler.node.type.core.ScalarType;
import org.voidlang.compiler.node.type.core.Type;
import org.voidlang.compiler.node.type.named.NamedScalarType;
import org.voidlang.compiler.node.value.Value;
import org.voidlang.llvm.element.*;

@RequiredArgsConstructor
@Getter
@NodeInfo(type = NodeType.ACCESSOR)
public class Accessor extends Value implements Loadable {
    @NotNull
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
            String fieldName = name.getFieldName();

            Type valueType = value.getValueType();
            if (valueType instanceof NamedScalarType named) {
                QualifiedName name = ((ScalarType) named.getScalarType()).getName();
                valueType = resolveType(name.getDirect());
            } else if (valueType instanceof ScalarType scalar) {
                valueType = resolveType(scalar.getName().getDirect());
            }

            if (!(valueType instanceof Element element))
                throw new IllegalStateException("Trying to access field '" + fieldName + "' of a non-element type "
                    + valueType + ": " + valueType.getClass().getSimpleName());

            IRStruct rootType = (IRStruct) element.generateType(context);
            Field field = element.resolveField(name.getFieldName());
            if (field == null)
                throw new IllegalStateException("No such field '" + fieldName + "' in type " + element);

            return builder.structMemberPointer(rootType, instance, field.getFieldIndex(), "struct (gen) " + field.getName());
        }

        return value.generate(generator);
    }

    @Override
    public IRValue generateAndLoad(Generator generator) {
        if (value instanceof PointerOwner owner && owner.getValueType() instanceof PassedByReference
                && !getName().isFieldAccess())
            return owner.getPointer();

        else if (value instanceof Loadable loadable && !getName().isFieldAccess())
            return loadable.load(generator);

        IRContext context = generator.getContext();
        IRBuilder builder = generator.getBuilder();

        if (name.isFieldAccess()) {
            PointerOwner owner = (PointerOwner) value;
            IRValue instance = owner.getPointer();
            String fieldName = name.getFieldName();

            Type valueType = value.getValueType();
            if (valueType instanceof NamedScalarType named) {
                QualifiedName name = ((ScalarType) named.getScalarType()).getName();
                valueType = resolveType(name.getDirect());
            } else if (valueType instanceof ScalarType scalar) {
                valueType = resolveType(scalar.getName().getDirect());
            }

            if (!(valueType instanceof Element element))
                throw new IllegalStateException("Trying to access field '" + fieldName + "' of a non-element type " + valueType);

            IRStruct rootType = (IRStruct) element.generateType(context);
            Field field = element.resolveField(fieldName);
            if (field == null)
                throw new IllegalStateException("No such field '" + fieldName + "' in type " + element);

            IRValue pointer = builder.structMemberPointer(rootType, instance, field.getFieldIndex(), "struct (load) " + field.getName());
            IRType fieldType = field.getType().generateType(context);

            return builder.load(fieldType, pointer, "field load " + fieldName);
        }

        return value.generate(generator);
    }

    @Override
    public IRValue generateNamed(Generator generator, String localName) {
        if (value instanceof PointerOwner owner && owner.getValueType() instanceof PassedByReference
                && !getName().isFieldAccess())
            return owner.getPointer();

        else if (value instanceof Loadable loadable && !getName().isFieldAccess())
            return loadable.load(generator);

        IRContext context = generator.getContext();
        IRBuilder builder = generator.getBuilder();

        if (getName().isFieldAccess()) {
            PointerOwner owner = (PointerOwner) value;
            IRValue instance = owner.getPointer();
            String fieldName = getName().getFieldName();

            Type valueType = value.getValueType();
            if (valueType instanceof NamedScalarType named) {
                QualifiedName qualifiedName = ((ScalarType) named.getScalarType()).getName();
                valueType = resolveType(qualifiedName.getDirect());
            } else if (valueType instanceof ScalarType scalar) {
                valueType = resolveType(scalar.getName().getDirect());
            }

            if (!(valueType instanceof Element element))
                throw new IllegalStateException("Trying to access field '" + fieldName + "' of a non-element type " + valueType);

            IRStruct rootType = (IRStruct) element.generateType(context);
            Field field = element.resolveField(fieldName);
            if (field == null)
                throw new IllegalStateException("No such field '" + fieldName + "' in type " + element);

            IRValue pointer = builder.structMemberPointer(rootType, instance, field.getFieldIndex(), "struct (load) " + field.getName());
            IRType fieldType = field.getType().generateType(context);

            return builder.load(fieldType, pointer, localName);

            // return builder.structMemberPointer(rootType, instance, field.getFieldIndex(), localName);
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
        if (name.isFieldAccess()) {
            PointerOwner owner = (PointerOwner) value;
            IRValue instance = owner.getPointer();
            String fieldName = name.getFieldName();

            Type valueType = value.getValueType();
            if (valueType instanceof NamedScalarType named) {
                QualifiedName name = ((ScalarType) named.getScalarType()).getName();
                valueType = resolveType(name.getDirect());
            } else if (valueType instanceof ScalarType scalar) {
                valueType = resolveType(scalar.getName().getDirect());
            }

            if (!(valueType instanceof Element element))
                throw new IllegalStateException(
                    "Trying to access field '" + fieldName + "' of a non-element type " + valueType +
                    " of " + valueType.getClass().getSimpleName()
                );

            Field field = element.resolveField(fieldName);
            if (field == null)
                throw new IllegalStateException("No such field '" + fieldName + "' in type " + element);

            return field.getResolvedType();
        }

        return value.getValueType();
    }

    @Override
    public String toString() {
        return name.toString();
    }
}
