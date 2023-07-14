package org.voidlang.compiler.node.local;

import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.type.core.Type;
import org.voidlang.llvm.element.IRType;
import org.voidlang.llvm.element.IRValue;

public interface PointerOwner extends Loadable {
    IRValue getPointer();

    IRType getPointerType();

    Type getValueType();

    default IRValue load(Generator generator) {
        return ((Node) this).generateAndLoad(generator);
    }

    default IRValue store(IRValue value, Generator generator) {
        return null;
    }
}
