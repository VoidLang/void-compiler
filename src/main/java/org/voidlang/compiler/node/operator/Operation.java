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
import org.voidlang.llvm.element.Comparator;
import org.voidlang.llvm.element.IRBuilder;
import org.voidlang.llvm.element.IRValue;

/**
 * Represents an operation between two values in the Abstract Syntax Tree.
 * The order of the operations will be determined by their precedence defined in
 * {@link Operator#getPrecedence()} and the operation tree be transformed by the parser.
 * <p>Example:</p>
 * <pre> {@code
 *     1 + 2 * 3
 * } </pre>
 * The previous code will resolve to {@code (1 + 2) * 3}, according to the tree parsing.
 * However, the transformer will convert this operation to {@code 1 + (2 * 3)}.
 */
@AllArgsConstructor
@Getter
@Setter
@NodeInfo(type = NodeType.OPERATION)
public class Operation extends Value {
    /**
     * The left operand target of the operation.
     */
    @NonNull
    private Value left;

    /**
     * The target operator of the operation.
     */
    private final Operator operator;

    /**
     * The right operand target of the operation.
     */
    @NonNull
    private Value right;

    /**
     * Initialize all the child nodes for the overriding node.
     * @param parent parent node of the overriding node
     */
    @Override
    public void preProcess(Node parent) {
        this.parent = parent;
        left.preProcess(this);
        right.preProcess(this);
    }

    /**
     * Initialize all type declarations for the overriding node.
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessType(Generator generator) {
        left.postProcessType(generator);
        right.postProcessType(generator);
    }

    /**
     * Initialize all class member declarations for the overriding node.
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessMember(Generator generator) {
        left.postProcessMember(generator);
        right.postProcessMember(generator);
    }

    /**
     * Initialize all type uses for the overriding node.
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessUse(Generator generator) {
        left.postProcessUse(generator);
        right.postProcessUse(generator);
    }

    /**
     * Generate an LLVM instruction for this node
     * @param generator LLVM instruction generation context
     */
    @Override
    public IRValue generate(Generator generator) {
        IRBuilder builder = generator.getBuilder();
        IRValue left = getLeft().generateAndLoad(generator);
        IRValue right = getRight().generateAndLoad(generator);
        return switch (operator) {
            case ADD -> builder.add(left, right, "add");
            case SUBTRACT -> builder.subtract(left, right, "sub");
            case MULTIPLY -> builder.multiply(left, right, "mul");

            case EQUAL -> builder.compareInt(Comparator.INTEGER_EQUAL, left, right, "eq");
            case NOT_EQUAL -> builder.compareInt(Comparator.INTEGER_NOT_EQUAL, left, right, "neq");

            case GREATER_THAN -> builder.compareInt(Comparator.SIGNED_INTEGER_GREATER_THAN, left, right, "gt");
            case GREATER_OR_EQUAL -> builder.compareInt(Comparator.SIGNED_INTEGER_GREATER_OR_EQUAL, left, right, "gte");

            case LESS_THAN -> builder.compareInt(Comparator.SIGNED_INTEGER_LESS_THAN, left, right, "lt");
            case LESS_OR_EQUAL -> builder.compareInt(Comparator.SIGNED_INTEGER_LESS_OR_EQUAL, left, right, "lte");

            case AND -> builder.and(left, right, "and");
            case OR -> builder.or(left, right, "or");

            default -> throw new IllegalStateException("Unable to generate complex operator for " + operator);
        };
    }

    /**
     * Get the wrapped type of this value.
     * @return wrapped value type
     */
    @Override
    public Type getValueType() {
        // TODO assert two types equals, if not try to convert
        return left.getValueType();
    }

    @Override
    public String toString() {
        return left + " " + operator.getValue() + " " + right;
    }
}
