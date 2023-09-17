package org.voidlang.compiler.node.type.pointer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class Pointer {
    private final PointerType type;

    private final int dimensions;

    @Override
    public String toString() {
        return switch (type) {
            case NONE -> "";
            case REFERENCE -> "&";
            case POINTER -> "*".repeat(dimensions);
        };
    }

    public static Pointer none() {
        return new Pointer(PointerType.NONE, 0);
    }

    public static Pointer reference() {
        return new Pointer(PointerType.REFERENCE, 0);
    }

    public static Pointer pointer(int dimensions) {
        return new Pointer(PointerType.POINTER, dimensions);
    }
}
