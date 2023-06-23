package org.voidlang.compiler.node.local;

import org.voidlang.compiler.node.Generator;
import org.voidlang.llvm.element.IRValue;

public interface Loadable {
    IRValue load(Generator generator);
}
