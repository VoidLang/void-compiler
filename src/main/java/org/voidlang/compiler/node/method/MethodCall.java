package org.voidlang.compiler.node.method;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeInfo;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.compiler.node.element.Class;
import org.voidlang.compiler.node.element.Method;
import org.voidlang.compiler.node.local.Loadable;
import org.voidlang.compiler.node.type.QualifiedName;
import org.voidlang.compiler.node.type.core.ScalarType;
import org.voidlang.compiler.node.type.core.Type;
import org.voidlang.compiler.node.type.named.NamedScalarType;
import org.voidlang.compiler.node.value.Value;
import org.voidlang.llvm.element.IRBuilder;
import org.voidlang.llvm.element.IRContext;
import org.voidlang.llvm.element.IRModule;
import org.voidlang.llvm.element.IRValue;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Getter
@NodeInfo(type = NodeType.METHOD_CALL)
public class MethodCall extends Value {
    private final QualifiedName name;
    private final List<Value> arguments;

    private Method method;

    /**
     * Initialize all the child nodes for the overriding node.
     * @param parent parent node of the overriding node
     */
    @Override
    public void preProcess(Node parent) {
        this.parent = parent;
        for (Value node : arguments)
            node.preProcess(this);
    }

    /**
     * Initialize all type declarations for the overriding node.
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessType(Generator generator) {
        for (Node node : arguments)
            node.postProcessType(generator);
    }

    /**
     * Initialize all class member declarations for the overriding node.
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessMember(Generator generator) {
        for (Node node : arguments)
            node.postProcessMember(generator);
    }

    /**
     * Initialize all type uses for the overriding node.
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessUse(Generator generator) {
        for (Value node : arguments)
            node.postProcessUse(generator);

        List<Type> argTypes = arguments
            .stream()
            .map(Value::getValueType)
            .toList();

        String methodName = name.getDirect();
        method = resolveMethod(methodName, argTypes);
        if (method == null) {
            String args = argTypes.stream()
                .map(type -> {
                    if (type instanceof Class clazz)
                        return clazz.getName();
                    else if (type instanceof ScalarType scalar)
                        return scalar.getName().toString();
                    return type.getClass().getSimpleName();
                })
                .collect(Collectors.joining(", "));
            throw new IllegalStateException("Unable to resolve method " + methodName + "(" + args + ")");
        }
    }

    /**
     * Generate an LLVM instruction for this node
     * @param generator LLVM instruction generation context
     */
    @Override
    public IRValue generate(Generator generator) {
        // extract the context from the generator
        IRContext context = generator.getContext();
        IRModule module = generator.getModule();
        IRBuilder builder = generator.getBuilder();

        return builder.call(method.getFunction(), arguments
            .stream()
            .map(arg -> arg.generateAndLoad(generator))
            .toList(), "call " + method.getName());
    }

    @Override
    public IRValue generateNamed(Generator generator, String name) {
        // extract the context from the generator
        IRContext context = generator.getContext();
        IRModule module = generator.getModule();
        IRBuilder builder = generator.getBuilder();

        return builder.call(method.getFunction(), arguments
            .stream()
            .map(arg -> arg.generateAndLoad(generator))
            .toList(), name);
    }

    /**
     * Get the wrapped type of this value.
     * @return wrapped value type
     */
    @Override
    public Type getValueType() {
        return method.getReturnType();
    }
}
