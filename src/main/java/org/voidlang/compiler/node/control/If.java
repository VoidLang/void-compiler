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
     * Initialize all the child nodes for the overriding node.
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

    /**
     * Initialize all type declarations for the overriding node.
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessType(Generator generator) {
        condition.postProcessType(generator);
        for (Node node : body)
            node.postProcessType(generator);
        for (ElseIf elseIf : elseIfs)
            elseIf.postProcessType(generator);
        if (elseCase != null)
            elseCase.postProcessType(generator);
    }

    /**
     * Initialize all type uses for the overriding node.
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessUse(Generator generator) {
        condition.postProcessUse(generator);
        for (Node node : body)
            node.postProcessUse(generator);
        for (ElseIf elseIf : elseIfs)
            elseIf.postProcessUse(generator);
        if (elseCase != null)
            elseCase.postProcessUse(generator);
    }

    /**
     * Generate an LLVM instruction for this node
     * @param generator LLVM instruction generation context
     */
    @Override
    public IRValue generate(Generator generator) {
        IRBuilder builder = generator.getBuilder();

        IRBlock ifBlock = IRBlock.create(getContext().getFunction(), "if");
        IRBlock merge = IRBlock.create(getContext().getFunction(), "merge");

        IRValue condition = getCondition().generate(generator);
        builder.jumpIf(condition, ifBlock, merge);

        builder.positionAtEnd(ifBlock);
        for (Node node : body)
            node.generate(generator);
        // jump to the merge block if the last statement of the IF block
        // is not a return statement TODO also check for GOTO
        if (body.isEmpty() || !body.get(body.size() - 1).is(NodeType.RETURN))
            builder.jump(merge);

        builder.positionAtEnd(merge);

        return null;
    }
}
