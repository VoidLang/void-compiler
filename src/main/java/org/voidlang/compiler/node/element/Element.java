package org.voidlang.compiler.node.element;

import org.voidlang.compiler.node.Node;
import org.voidlang.llvm.element.IRContext;
import org.voidlang.llvm.element.IRType;

import java.util.List;

public abstract class Element extends Scope {
    public Element(List<Node> body) {
        super(body);
    }

    /**
     * Generate an LLVM type for this type element type.
     * @param context LLVM module context
     * @return type ir code wrapper
     */
    public abstract IRType generateType(IRContext context);
}
