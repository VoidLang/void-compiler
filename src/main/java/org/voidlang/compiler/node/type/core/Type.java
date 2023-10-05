package org.voidlang.compiler.node.type.core;

import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.type.QualifiedName;
import org.voidlang.compiler.node.type.array.Array;
import org.voidlang.compiler.node.type.generic.GenericArgumentList;
import org.voidlang.compiler.node.type.named.NamedScalarType;
import org.voidlang.compiler.node.type.pointer.Referencing;
import org.voidlang.llvm.element.IRContext;
import org.voidlang.llvm.element.IRType;
import org.voidlang.llvm.element.IRValue;

/**
 * Represents an entry which may be a {@link ScalarType} or a {@link CompoundType}.
 * The purpose of this class is to be able to hold type groups <strong>recursively</strong>.
 * <p>Examples:</p>
 * <pre> {@code
 *     float foo
 *     (int, bool)
 *     int |float, string|
 * } </pre>
 * The code {@code float} will be a {@link ScalarType}, as it does not have any members, and {@code (int, bool)}
 * will be a {@link CompoundType}, as it has two members inside.
 * @see ScalarType
 * @see CompoundType
 */
public interface Type {
    /**
     * The type wrapper for the "let" keyword.
     */
    Type LET = primitive("let");

    /**
     * The type wrapper for the "mut" keyword.
     */
    Type MUT = primitive("mut");

    /**
     * The type wrapper for the "mut" keyword.
     */
    Type REF = primitive("ref");

    /**
     * The type wrapper for a signed 8-bit integer.
     */
    Type BYTE = primitive("byte");

    /**
     * The type wrapper for an unsigned 8-bit integer.
     */
    Type UBYTE = primitive("ubyte");

    /**
     * The type wrapper for a signed 16-bit integer.
     */
    Type SHORT = primitive("short");

    /**
     * The type wrapper for an unsigned 16-bit integer.
     */
    Type USHORT = primitive("ushort");

    /**
     * The type wrapper for a signed 32-bit integer.
     */
    Type INT = primitive("int");

    /**
     * The type wrapper for an unsigned 32-bit integer.
     */
    Type UINT = primitive("uint");

    /**
     * The type wrapper for a signed 64-bit integer.
     */
    Type LONG = primitive("long");

    /**
     * The type wrapper for an unsigned 64-bit integer.
     */
    Type ULONG = primitive("ulong");

    /**
     * The type wrapper for a 1-bit boolean.
     */
    Type BOOL = primitive("bool");

    /**
     * The type wrapper for a 32-bit floating point number.
     */
    Type FLOAT = primitive("float");

    /**
     * The type wrapper for a 64-bit floating point number.
     */
    Type DOUBLE = primitive("double");

    /**
     * The type wrapper for a const char* string.
     */
    Type STR = new ScalarType(
        Referencing.reference(1),
        QualifiedName.primitive("byte"),
        GenericArgumentList.implicit(),
        Array.noArray()
    );
    
    /**
     * Indicate, whether this entry is a {@link ScalarType}, so it does not have any nested members.
     * @return true if this type entry is a direct type
     */
    default boolean isScalar() {
        return this instanceof ScalarType;
    }

    /**
     * Indicate, whether this entry is a {@link CompoundType}, so it has nested members only.
     * @return true if this type entry is a group of type entries
     */
    default boolean isCompound() {
        return this instanceof CompoundType;
    }

    /**
     * Indicate, whether this entry is a {@link LambdaType}, so it has nested parameter types.
     * @return true if this type entry is a callable lambda function
     */
    default boolean isLambda() {
        return this instanceof LambdaType;
    }

    /**
     * Indicate, how the type should be referenced as.
     * @return type referencing
     */
    Referencing getReferencing();

    /**
     * Create a new type wrapper for the specified primitive type.
     * @param type primitive type name
     * @return primitive type wrapper
     */
    static Type primitive(String type) {
        return new ScalarType(
            Referencing.none(),
            QualifiedName.primitive(type),
            GenericArgumentList.implicit(),
            Array.noArray()
        );
    }

    /**
     * Generate an LLVM type for this type wrapper
     * @param context LLVM module context
     * @return type ir code wrapper
     */
    IRType generateType(IRContext context);

    /**
     * Get the default value of this type.
     * @param generator LLVM code generator
     * @return the default value of this type
     */
    default IRValue defaultValue(Generator generator) {
        if (!(this instanceof NamedScalarType type))
            return null;
        IRType irType = generateType(generator.getContext());
        ScalarType scalar = (ScalarType) type.getScalarType();
        return switch (scalar.getName().getTypes().get(0).getValue()) {
            case "bool", "byte", "ubyte", "short", "ushort", "int", "uint", "long",
                    "ulong", "float", "double" -> irType.constInt(0);
            default -> null;
        };
    }
}
