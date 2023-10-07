package org.voidlang.compiler.node.memory;

import org.voidlang.compiler.node.Generator;
import org.voidlang.llvm.element.IRType;
import org.voidlang.llvm.element.IRValue;

public interface HeapAllocator {
    IRValue allocateHeap(Generator generator, String name);

    default IRValue allocateHeapAnonymous(Generator generator) {
        return allocateHeap(generator, "anonymous malloc");
    }

    IRType getPointerType();
}
