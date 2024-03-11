package org.voidlang.compiler.node;

import org.voidlang.compiler.node.element.Method;

public interface FunctionContext {
    void setContext(Method context);

    Method getContext();
}
