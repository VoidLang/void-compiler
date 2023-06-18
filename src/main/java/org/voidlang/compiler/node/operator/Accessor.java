package org.voidlang.compiler.node.operator;

import lombok.Getter;
import org.voidlang.compiler.Instruction;
import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.compiler.node.type.QualifiedName;
import org.voidlang.llvm.element.IRValue;

@Getter
public class Accessor extends Instruction {
    private final QualifiedName name;

    public Accessor(QualifiedName name) {
        super(NodeType.ACCESSOR);
        this.name = name;
    }

    /**
     * Generate an LLVM instruction for this node
     *
     * @param generator LLVM instruction generation context
     */
    @Override
    public IRValue generate(Generator generator) {
        return null;
    }
}
