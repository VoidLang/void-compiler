package org.voidlang.compiler.node.control;

import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.element.Field;
import org.voidlang.compiler.node.type.core.Type;

public abstract class Element extends Node implements Type {
    public abstract Field resolveField(String name);
}
