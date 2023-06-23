package org.voidlang.compiler.node;

import lombok.Getter;
import lombok.Setter;
import org.voidlang.compiler.node.element.Method;

@Getter
@Setter
public abstract class Instruction extends Node {
    private transient Method context;
}
