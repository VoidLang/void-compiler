package org.voidlang.compiler.node.control;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.voidlang.compiler.node.*;
import org.voidlang.llvm.element.IRBlock;
import org.voidlang.llvm.element.IRBuilder;
import org.voidlang.llvm.element.IRValue;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Getter
@NodeInfo(type = NodeType.IF)
public class If extends Instruction {
    private final Node condition;

    private final List<Node> body;

    private final List<ElseIf> elseIfs = new ArrayList<>();

    @Setter
    private Else elseCase;

    /**
     * Generate an LLVM instruction for this node
     * @param generator LLVM instruction generation context
     */
    @Override
    public IRValue generate(Generator generator) {
        IRBuilder builder = generator.getBuilder();

        IRBlock ifBlock = IRBlock.create(getContext().getFunction(), "if");
        IRBlock exit = IRBlock.create(getContext().getFunction(), "exit");

        IRValue condition = getCondition().generate(generator);
        builder.jumpIf(condition, ifBlock, exit);

        builder.positionAtEnd(ifBlock);
        for (Node node : body)
            node.generate(generator);

        builder.positionAtEnd(exit);

        return null;
    }

    /**
     * Initialize all the child nodes for this node.
     * @param parent parent node of the overriding node
     */
    @Override
    public void preProcess(Node parent) {
        this.parent = parent;
        condition.preProcess(this);
        for (Node node : body)
            node.preProcess(this);
        // else cases should inherit the parent of IF as a parent,
        // as they are at the same scope level as the IF statement
        for (ElseIf elseIf : elseIfs)
            elseIf.preProcess(parent);
        if (elseCase != null)
            elseCase.preProcess(parent);
    }
}
