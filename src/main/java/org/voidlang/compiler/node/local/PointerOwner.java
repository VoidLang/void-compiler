package org.voidlang.compiler.node.local;

import org.voidlang.compiler.node.type.core.Type;
import org.voidlang.llvm.element.IRType;
import org.voidlang.llvm.element.IRValue;

public interface PointerOwner {
    IRValue getPointer();

    IRType getPointerType();

    Type getValueType();
}
