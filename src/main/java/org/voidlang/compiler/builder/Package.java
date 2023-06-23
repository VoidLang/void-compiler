package org.voidlang.compiler.builder;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeInfo;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.compiler.node.element.Method;
import org.voidlang.compiler.node.type.core.Type;
import org.voidlang.llvm.element.IRValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@NodeInfo(type = NodeType.ROOT)
public class Package extends Node {
    private final Map<String, List<Method>> methods = new HashMap<>();

    /**
     * Generate an LLVM instruction for this node
     * @param generator LLVM instruction generation context
     */
    @Override
    public IRValue generate(Generator generator) {
        return null;
    }

    @Override
    @Nullable
    public Method resolveMethod(String name, List<Type> types) {
        List<Method> methodList = methods.get(name);
        if (methodList == null)
            return null;
        for (Method method : methodList) {
            if (method.checkTypes(types))
                return method;
        }
        return null;
    }

    public void defineMethod(Method method) {
        methods
            .computeIfAbsent(method.getName(), name -> new ArrayList<>())
            .add(method);
    }
}
