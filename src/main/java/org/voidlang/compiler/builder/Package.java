package org.voidlang.compiler.builder;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeInfo;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.compiler.node.element.Class;
import org.voidlang.compiler.node.element.Method;
import org.voidlang.compiler.node.type.core.Type;
import org.voidlang.llvm.element.IRValue;

import java.util.*;

@RequiredArgsConstructor
@Getter
@NodeInfo(type = NodeType.ROOT)
public class Package extends Node {
    private final Map<String, List<Method>> methods = new HashMap<>();

    private final Map<String, Class> classes = new HashMap<>();

    private final Generator generator;

    /**
     * Generate an LLVM instruction for this node
     * @param generator LLVM instruction generation context
     */
    @Override
    public IRValue generate(Generator generator) {
        return null;
    }

    /**
     * Initialize all the child nodes for the overriding node.
     * @param parent parent node of the overriding node
     */
    @Override
    public void preProcess(Node parent) {
        this.parent = parent;
        for (List<Method> methodList : methods.values()) {
            for (Method method : methodList)
                method.preProcess(this);
        }
        for (Class clazz : classes.values())
            clazz.preProcess(this);
    }

    /**
     * Initialize all type declarations for the overriding node.
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessType(Generator generator) {
        for (List<Method> methodList : methods.values()) {
            for (Method method : methodList)
                method.postProcessType(generator);
        }
        for (Class clazz : classes.values())
            clazz.postProcessType(generator);
    }

    /**
     * Initialize all class member declarations for the overriding node.
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessMember(Generator generator) {
        for (List<Method> methodList : methods.values()) {
            for (Method method : methodList)
                method.postProcessMember(generator);
        }
        for (Class clazz : classes.values())
            clazz.postProcessMember(generator);
    }

    /**
     * Initialize all type uses for the overriding node.
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessUse(Generator generator) {
        for (List<Method> methodList : methods.values()) {
            for (Method method : methodList)
                method.postProcessUse(generator);
        }
        for (Class clazz : classes.values())
            clazz.postProcessUse(generator);
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

    @Override
    public @Nullable Type resolveType(String name) {
        return classes.get(name);
    }

    public void defineMethod(Method method) {
        methods
            .computeIfAbsent(method.getName(), name -> new ArrayList<>())
            .add(method);
    }

    public void defineClass(Class clazz) {
        classes.put(clazz.getName(), clazz);
    }
}
