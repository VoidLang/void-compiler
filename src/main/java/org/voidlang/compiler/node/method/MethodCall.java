package org.voidlang.compiler.node.method;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeInfo;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.compiler.node.element.Method;
import org.voidlang.compiler.node.local.Loadable;
import org.voidlang.compiler.node.type.QualifiedName;
import org.voidlang.compiler.node.type.core.Type;
import org.voidlang.compiler.node.value.Value;
import org.voidlang.llvm.element.IRBuilder;
import org.voidlang.llvm.element.IRContext;
import org.voidlang.llvm.element.IRModule;
import org.voidlang.llvm.element.IRValue;

import java.util.List;

@RequiredArgsConstructor
@Getter
@NodeInfo(type = NodeType.METHOD_CALL)
public class MethodCall extends Value {
    private final QualifiedName name;
    private final List<Value> arguments;

    private Method method;

    @Override
    public void preProcess(Node root) {
        super.preProcess(root);
    }

    @Override
    public void postProcessUse(Generator generator) {
        super.postProcessUse(generator);
        List<Type> argTypes = arguments
            .stream()
            .map(Value::getValueType)
            .toList();
        method = resolveMethod(name.getDirect(), argTypes);
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
            .map(arg -> {
                if (arg instanceof Loadable loadable)
                    return loadable.load(generator);
                return arg.generate(generator);
            })
            .toList());
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
