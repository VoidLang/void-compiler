package org.voidlang.compiler.node.operator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.voidlang.compiler.node.type.QualifiedName;
import org.voidlang.compiler.node.type.array.Array;
import org.voidlang.compiler.node.type.core.ScalarType;
import org.voidlang.compiler.node.type.core.Type;
import org.voidlang.compiler.node.type.generic.GenericArgumentList;
import org.voidlang.compiler.node.type.pointer.Referencing;

import java.util.stream.Stream;

@RequiredArgsConstructor
@Getter
public enum PrimitiveType {
    BOOLEAN(0, false),
    CHAR(1, false),
    BYTE(2, false),
    UBYTE(2, false),
    SHORT(3, false),
    USHORT(3, false),
    INT(4, false),
    UINT(4, false),
    LONG(5, false),
    ULONG(5, false),
    FLOAT(6, false),
    DOUBLE(7, false),
    UNKNOWN(-1, false);

    private final int precedence;
    private final boolean floating;

    public Type toType() {
        return new ScalarType(
            Referencing.none(),
            QualifiedName.primitive(name().toLowerCase()),
            GenericArgumentList.implicit(),
            Array.noArray()
        );
    }

    public static PrimitiveType of(ScalarType type) {
        return
            Stream.of(values())
                .filter(t -> t.name().equals(type.getName().getPrimitive().toUpperCase()))
                .findFirst()
                .orElse(UNKNOWN);
    }
}
