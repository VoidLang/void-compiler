package org.voidlang.compiler.node.control;

import org.jetbrains.annotations.Nullable;
import org.voidlang.compiler.Instruction;
import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.llvm.element.IRBuilder;
import org.voidlang.llvm.element.IRValue;

public class Return extends Instruction {
    @Nullable
    private final Node value;

    public Return(@Nullable Node value) {
        super(NodeType.RETURN);
        this.value = value;
    }

    /**
     * Generate an LLVM instruction for this node
     * @param generator LLVM instruction generation context
     */
    @Override
    public IRValue generate(Generator generator) {
        IRBuilder builder = generator.getBuilder();
        if (value != null)
            return builder.returnValue(value.generate(generator));
        else
            return builder.returnVoid();
    }
}
