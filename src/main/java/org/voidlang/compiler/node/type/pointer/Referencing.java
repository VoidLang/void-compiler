package org.voidlang.compiler.node.type.pointer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class Referencing {
    private final ReferencingType type;

    private final int dimensions;

    @Override
    public String toString() {
        return switch (type) {
            case NONE -> "";
            case REFERENCE -> "ref" + "*".repeat(dimensions - 1) + " ";
            case DEREFERENCE -> "deref" + "*".repeat(dimensions - 1) + " ";
            case MUTABLE -> "mut ";
        };
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Referencing other))
            return false;
        return type == other.type && dimensions == other.dimensions;
    }

    public static Referencing none() {
        return new Referencing(ReferencingType.NONE, 0);
    }

    public static Referencing reference(int dimensions) {
        return new Referencing(ReferencingType.REFERENCE, dimensions);
    }

    public static Referencing mutable() {
        return new Referencing(ReferencingType.MUTABLE, 0);
    }

    public static Referencing dereference(int dimensions) {
        return new Referencing(ReferencingType.DEREFERENCE, dimensions);
    }
}
