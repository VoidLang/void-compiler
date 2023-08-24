package org.voidlang.compiler.node.local;

import org.voidlang.compiler.node.Generator;
import org.voidlang.llvm.element.IRType;
import org.voidlang.llvm.element.IRValue;

public interface Allocator {
    IRValue allocate(Generator generator, String name);

    default IRValue allocateAnonymous(Generator generator) {
        return allocate(generator, "anonymous alloc");
    }

    IRType getPointerType();
}
