package org.voidlang.compiler.node.control;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.voidlang.compiler.node.*;
import org.voidlang.compiler.node.local.ImmutableLocalDeclareAssign;
import org.voidlang.compiler.node.local.MutableLocalDeclareAssign;
import org.voidlang.compiler.node.value.Value;
import org.voidlang.llvm.element.IRBlock;
import org.voidlang.llvm.element.IRBuilder;
import org.voidlang.llvm.element.IRValue;

import java.util.List;

@RequiredArgsConstructor
@Getter
@NodeInfo(type = NodeType.WHILE)
public class While extends Instruction {
    private final Node condition;

    private final List<Node> body;

    /**
     * Initialize all the child nodes for the overriding node.
     * @param parent parent node of the overriding node
     */
    @Override
    public void preProcess(Node parent) {
        this.parent = parent;
        if (condition instanceof FunctionContext context)
            context.setContext(getContext());
        condition.preProcess(this);
        for (Node node : body) {
            if (node instanceof FunctionContext context)
                context.setContext(getContext());
            node.preProcess(this);
        }
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
    }

    /**
     * Initialize all class member declarations for the overriding node.
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessMember(Generator generator) {
        condition.postProcessMember(generator);
        for (Node node : body)
            node.postProcessMember(generator);
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
    }

    /**
     * Resolve a node from this node context by its name. If the name is unresolved locally,
     * the parent element tries to resolve it.
     * @param name target node name
     * @return resolved node or null if it was not found
     */
    @Override
    public @Nullable Value resolveName(String name) {
        // resolve local variables in the method body
        for (Node node : body) {
            if (node instanceof ImmutableLocalDeclareAssign local && local.getName().equals(name))
                return local;
            else if (node instanceof MutableLocalDeclareAssign local && local.getName().equals(name))
                return local;
        }

        // let the parent nodes recursively resolve the name
        return super.resolveName(name);
    }

    /**
     * Generate an LLVM instruction for this node
     * @param generator LLVM instruction generation context
     */
    @Override
    public IRValue generate(Generator generator) {
        IRBuilder builder = generator.getBuilder();

        // create a block that will test if the condition is valid, if so it jumps to the
        // 'while' block's body, or else it jumps to merge and terminates the execution
        IRBlock test = IRBlock.create(getContext().getFunction(), "while");

        // create a block that will execute the instructions that are inside the 'while' statement's body
        IRBlock loop = IRBlock.create(getContext().getFunction(), "do");

        // create a block that the 'test' block will jump to, if the condition turns false
        IRBlock merge = IRBlock.create(getContext().getFunction(), "merge");

        // jump to the 'while' block's condition and begin testing
        builder.jump(test);

        // test the condition and execute the loop body if true, exit the loop otherwise
        builder.positionAtEnd(test);
        IRValue condition = getCondition().generate(generator);
        builder.jumpIf(condition, loop, merge);

        // execute the body of the loop and return to the condition
        builder.positionAtEnd(loop);
        for (Node node : body)
            node.generate(generator);

        // jump to the while block if the last statement of the block
        // is not a return statement TODO also check for GOTO
        if (body.isEmpty() || !body.get(body.size() - 1).is(NodeType.RETURN))
            builder.jump(test);

        // let all remaining instructions to be assigned for the merge block
        builder.positionAtEnd(merge);

        return null;
    }
}
