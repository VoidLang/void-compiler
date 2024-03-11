package org.voidlang.compiler.node.value;

import lombok.Getter;
import lombok.Setter;
import org.voidlang.compiler.node.method.FunctionContext;
import org.voidlang.compiler.node.element.Method;

@Getter
@Setter
public abstract class FunctionContextValue extends Value implements FunctionContext {
    private Method context;
}
