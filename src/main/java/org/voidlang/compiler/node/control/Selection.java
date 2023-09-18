package org.voidlang.compiler.node.control;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeInfo;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.compiler.node.type.core.Type;
import org.voidlang.compiler.node.value.Value;
import org.voidlang.llvm.element.IRBuilder;
import org.voidlang.llvm.element.IRValue;

@RequiredArgsConstructor
@NodeInfo(type = NodeType.SELECTION)
public class Selection extends Value {
    @NotNull
    private final Value condition;

    @NotNull
    private final Value ifValue;

    @NotNull
    private final Value elseValue;

    /**
     * Generate an LLVM instruction for this node
     *
     * @param generator LLVM instruction generation context
     */
    @Override
    public IRValue generate(Generator generator) {
        IRBuilder builder = generator.getBuilder();
        return builder.select(
            condition.generateAndLoad(generator),
            ifValue.generateAndLoad(generator),
            elseValue.generateAndLoad(generator)
        );
    }

    /**
     * Initialize all the child nodes for the overriding node.
     *
     * @param parent parent node of the overriding node
     */
    @Override
    public void preProcess(Node parent) {
        this.parent = parent;
        condition.preProcess(this);
        ifValue.preProcess(this);
        elseValue.preProcess(this);
    }

    /**
     * Initialize all type declarations for the overriding node.
     *
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessType(Generator generator) {
        condition.postProcessType(generator);
        ifValue.postProcessType(generator);
        elseValue.postProcessType(generator);
    }

    /**
     * Initialize all class member declarations for the overriding node.
     *
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessMember(Generator generator) {
        condition.postProcessMember(generator);
        ifValue.postProcessMember(generator);
        elseValue.postProcessMember(generator);
    }

    /**
     * Initialize all type uses for the overriding node.
     *
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessUse(Generator generator) {
        condition.postProcessUse(generator);
        ifValue.postProcessUse(generator);
        elseValue.postProcessUse(generator);
    }

    /**
     * Get the wrapped type of this value.
     *
     * @return wrapped value type
     */
    @Override
    public Type getValueType() {
        return ifValue.getValueType();
    }
}
