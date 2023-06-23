package org.voidlang.compiler.node.operator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.NodeInfo;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.compiler.node.type.core.Type;
import org.voidlang.compiler.node.value.Value;
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
    private Value left;

    /**
     * The target operator of the operation.
     */
    private final Operator operator;

    /**
     * The right operand target of the operation.
     */
    private Value right;

    /**
     * Generate an LLVM instruction for this node
     * @param generator LLVM instruction generation context
     */
    @Override
    public IRValue generate(Generator generator) {
        IRBuilder builder = generator.getBuilder();
        IRValue left = getLeft().generate(generator);
        IRValue right = getRight().generate(generator);
        return switch (operator) {
            case ADD -> builder.add(left, right);
            case SUBTRACT -> builder.subtract(left, right);
            case MULTIPLY -> builder.multiply(left, right);
            default -> null;
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
}
