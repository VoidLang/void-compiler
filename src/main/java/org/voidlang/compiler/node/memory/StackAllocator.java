package org.voidlang.compiler.node.memory;

import org.voidlang.compiler.node.Generator;
import org.voidlang.llvm.element.IRType;
import org.voidlang.llvm.element.IRValue;

public interface StackAllocator {
    IRValue allocateStack(Generator generator, String name);

    default IRValue allocateStackAnonymous(Generator generator) {
        return allocateStack(generator, "anonymous alloc");
    }

    IRType getPointerType();
}
