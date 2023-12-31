package org.voidlang.compiler.node.control;

import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.element.Field;
import org.voidlang.compiler.node.element.Method;
import org.voidlang.compiler.node.type.core.Type;
import org.voidlang.llvm.element.IRStruct;
import org.voidlang.llvm.element.IRType;

import java.util.List;

public abstract class Element extends Node implements Type {
    public abstract Field resolveField(String name);

    public abstract IRStruct getStructType();

    public abstract IRType getPointerType();

    public abstract Method resolveMethod(String name, List<Type> types);
}
