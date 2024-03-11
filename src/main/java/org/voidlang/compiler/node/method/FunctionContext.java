package org.voidlang.compiler.node.method;

import org.voidlang.compiler.node.element.Method;

public interface FunctionContext {
    void setContext(Method context);

    Method getContext();
}
