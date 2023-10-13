package org.voidlang.compiler.node.type.core;

import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.voidlang.compiler.node.type.QualifiedName;
import org.voidlang.compiler.node.type.array.Array;
import org.voidlang.compiler.node.type.generic.GenericArgumentList;
import org.voidlang.compiler.node.type.named.NamedScalarType;
import org.voidlang.compiler.node.type.pointer.Referencing;
import org.voidlang.compiler.node.type.pointer.ReferencingType;
import org.voidlang.llvm.element.IRContext;
import org.voidlang.llvm.element.IRType;

/**
 * Represents a type use in the Void syntax that has a type token, generic arguments and array dimensions.
 * <p>Example:</p>
 * <pre> {@code
 *     MyCollection.Document<User>[] documents
 * } </pre>
 * Here {@code MyCollection.Document} is the type token, {@code <User>} is the generic token, and
 * {@code []} is a one-dimensional array specifier.
 */
@Getter
public class ScalarType implements Type {
    /**
     * The referencing type of the type.
     */
    @NonNull
    private final Referencing referencing;

    /**
     * The fully qualified name of the type.
     */
    @NotNull
    private final QualifiedName name;

    /**
     * The generic argument tokens of the type.
     */
    @NotNull
    private final GenericArgumentList generics;

    /**
     * The dimensions of the multidimensional array.
     */
    @NotNull
    private final Array array;

    public ScalarType(@NotNull Referencing referencing, @NotNull QualifiedName name,
                      @NotNull GenericArgumentList generics, @NotNull Array array) {
        this.referencing = referencing;
        this.name = name;
        this.generics = generics;
        this.array = array;
    }

    /**
     * Indicate, whether this type is an array.
     * @return true if there are array dimensions specified
     */
    public boolean isArray() {
        return !array.getDimensions().isEmpty();
    }

    /**
     * Get the string representation of the scalar type.
     * @return scalar type debug information
     */
    @Override
    public String toString() {
        return referencing.toString() + name + generics + array;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;

        if (o instanceof NamedScalarType named) {
            return referencing.equals(named.getReferencing())
                && this.equals(named.getScalarType());
        }

        if (!(o instanceof ScalarType type)) return false;

        if (!referencing.equals(type.referencing)) return false;
        if (!name.equals(type.name)) return false;
        if (!generics.equals(type.generics)) return false;
        return array.equals(type.array);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + generics.hashCode();
        result = 31 * result + array.hashCode();
        result = 31 * result + referencing.hashCode();
        return result;
    }

    /**
     * Generate an LLVM type for this type wrapper
     * @param context LLVM module context
     * @return type ir code wrapper
     */
    @Override
    public IRType generateType(IRContext context) {
        // TODO here probably shouldn't use void pointer, instead of pointer to type
        if (!name.isPrimitive())
            return IRType.pointerType(IRType.voidType(context));
        // convert the raw types to LLVM type representations
        IRType type = switch (name.getPrimitive()) {
            case "void" -> IRType.voidType(context);
            case "bool" -> IRType.int1(context);
            case "byte", "ubyte" -> IRType.int8(context);
            case "short", "ushort" -> IRType.int16(context);
            case "int", "uint" -> IRType.int32(context);
            case "long", "ulong" -> IRType.int64(context);
            case "float", "ufloat" -> IRType.floatType(context);
            case "double", "udouble" -> IRType.doubleType(context);
            default -> throw new IllegalStateException("Unknown primitive type " + name.getPrimitive());
        };
        // apply pointer types to the generated type
        if (referencing.getType() == ReferencingType.REFERENCE) {
            for (int i = 0; i < referencing.getDimensions(); i++)
                type = type.toPointerType();
        }
        return type;
    }
}
