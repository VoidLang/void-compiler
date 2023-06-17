package org.voidlang.compiler.node.operator;

import lombok.Getter;
import lombok.Setter;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.llvm.element.Builder;
import org.voidlang.llvm.element.Value;

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
@Getter
@Setter
public class Operation extends Node {
    /**
     * The left operand target of the operation.
     */
    private Node left;

    /**
     * The target operator of the operation.
     */
    private final Operator operator;

    /**
     * The right operand target of the operation.
     */
    private Node right;

    /**
     * Initialize the operation.
     * @param left left operand
     * @param operator target operator
     * @param right right operand
     */
    public Operation(Node left, Operator operator, Node right) {
        super(NodeType.OPERATION);
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    /**
     * Generate an LLVM instruction for this node
     * @param builder instruction builder for the current context
     * @return node ir code wrapper
     */
    @Override
    public Value generate(Builder builder) {
        return null;
    }
}
