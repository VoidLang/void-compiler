package org.voidlang.compiler.node.operator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeInfo;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.compiler.node.type.core.Type;
import org.voidlang.compiler.node.value.Value;
import org.voidlang.llvm.element.IRBuilder;
import org.voidlang.llvm.element.IRValue;

/**
 * Represents an operation of a single value in the Abstract Syntax Tree.
 * <p>Example:</p>
 * <pre> {@code
 *     -value
 * } </pre>
 */
@AllArgsConstructor
@Getter
@Setter
@NodeInfo(type = NodeType.SIDE_OPERATION)
public class SideOperation extends Value {
    /**
     * The target operator of the operation.
     */
    private final Operator operator;

    /**
     * The left operand target of the operation.
     */
    @NonNull
    private Value operand;

    /**
     * Generate an LLVM instruction for this node
     *
     * @param generator LLVM instruction generation context
     */
    @Override
    public IRValue generate(Generator generator) {
        IRBuilder builder = generator.getBuilder();
        IRValue operand = getOperand().generateAndLoad(generator);
        return switch (operator) {
            case NEGATE -> builder.negate(operand, "neg");
            case NOT -> builder.not(operand, "not");
            default -> throw new IllegalStateException("Unable to generate complex operator for " + operator);
        };
    }

    /**
     * Initialize all the child nodes for the overriding node.
     * @param parent parent node of the overriding node
     */
    @Override
    public void preProcess(Node parent) {
        this.parent = parent;
        operand.preProcess(this);
    }

    /**
     * Initialize all type declarations for the overriding node.
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessType(Generator generator) {
        operand.postProcessType(generator);
    }

    /**
     * Initialize all class member declarations for the overriding node.
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessMember(Generator generator) {
        operand.postProcessMember(generator);
    }

    /**
     * Initialize all type uses for the overriding node.
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessUse(Generator generator) {
        operand.postProcessUse(generator);
    }

    /**
     * Get the wrapped type of this value.
     *
     * @return wrapped value type
     */
    @Override
    public Type getValueType() {
        return operand.getValueType();
    }

    @Override
    public String toString() {
        return operator.getValue() + operand;
    }
}
