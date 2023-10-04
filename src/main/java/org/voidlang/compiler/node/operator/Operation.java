package org.voidlang.compiler.node.operator;

import lombok.*;
import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeInfo;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.compiler.node.type.core.ScalarType;
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
@Getter
@NodeInfo(type = NodeType.OPERATION)
public class Operation extends Value {
    /**
     * The left operand target of the operation.
     */
    @NonNull
    @Setter
    private Value left;

    /**
     * The target operator of the operation.
     */
    private final Operator operator;

    /**
     * The right operand target of the operation.
     */
    @NonNull
    @Setter
    private Value right;

    private PrimitiveType leftType, rightType, resultType;

    public Operation(@NonNull Value left, Operator operator, @NonNull Value right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }
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


        // check if non-scalar types are being operated
        if (!(getLeft().getValueType() instanceof ScalarType leftScalar))
            throw new IllegalStateException("Left operand of operation is not a scalar type: " + getLeft().getValueType());

        if (!(getRight().getValueType() instanceof ScalarType rightScalar))
            throw new IllegalStateException("Right operand of operation is not a scalar type: " + getRight().getValueType());

        // check if non-primitive types are being operated
        leftType = PrimitiveType.of(leftScalar);
        rightType = PrimitiveType.of(rightScalar);

        if (leftType == PrimitiveType.UNKNOWN || rightType == PrimitiveType.UNKNOWN)
            throw new IllegalStateException("Trying to do complex operation with non-primitive types: " + leftType + " " + rightType);

        resultType = leftType.getPrecedence() >= rightType.getPrecedence()
            ? leftType
            : rightType;
    }

    /**
     * Generate an LLVM instruction for this node
     * @param generator LLVM instruction generation context
     */
    @Override
    public IRValue generate(Generator generator) {
        IRBuilder builder = generator.getBuilder();

        // handle implicit casting in case of having different types
        // the type with the highest precedence will be used, the other will be cast to it
        IRValue left, right;
        // check if the LHS has higher precedence than the RHS
        if (leftType.getPrecedence() > rightType.getPrecedence()) {
            // implicitly cast the RHS to the LHS type
            right = new Casting(getRight(), leftType.toType())
                .generateAndLoad(generator);
            left = getLeft().generateAndLoad(generator);
        }
        // check if the RHS has higher precedence than the LHS
        else if (rightType.getPrecedence() > leftType.getPrecedence()) {
            // implicitly cast the LHS to the RHS type
            left = new Casting(getLeft(), rightType.toType())
                .generateAndLoad(generator);
            right = getRight().generateAndLoad(generator);
        }
        // if both types have the same precedence, then use the LHS type
        else {
            left = getLeft().generateAndLoad(generator);
            right = getRight().generateAndLoad(generator);
        }

        return switch (operator) {
            case ADD -> {
                if (resultType.isFloating())
                    yield builder.addFloat(left, right, "fadd");
                else
                    yield builder.add(left, right, "add");
            }

            case SUBTRACT, NEGATE -> {
                if (resultType.isFloating())
                    yield builder.subtractFloat(left, right, "fsub");
                else
                    yield builder.subtract(left, right, "sub");
            }

            case MULTIPLY -> {
                if (resultType.isFloating())
                    yield builder.multiplyFloat(left, right, "fmul");
                else
                    yield builder.multiply(left, right, "mul");
            }

            case EQUAL ->  {
                if (resultType.isFloating())
                    yield builder.compareFloat(Comparator.FLOAT_EQUAL_AND_NOT_NAN, left, right, "feq");
                else
                    yield builder.compareInt(Comparator.INTEGER_EQUAL, left, right, "eq");
            }

            case NOT_EQUAL -> {
                if (resultType.isFloating())
                    yield builder.compareFloat(Comparator.FLOAT_NOT_EQUAL_AND_NOT_NAN, left, right, "fneq");
                else
                    yield builder.compareInt(Comparator.INTEGER_NOT_EQUAL, left, right, "neq");
            }

            case GREATER_THAN -> {
                if (resultType.isFloating())
                    yield builder.compareFloat(Comparator.FLOAT_GREATER_THAN_AND_NOT_NAN, left, right, "fgt");
                else
                    yield builder.compareInt(Comparator.SIGNED_INTEGER_GREATER_THAN, left, right, "gt");
            }

            case GREATER_OR_EQUAL -> {
                if (resultType.isFloating())
                    yield builder.compareFloat(Comparator.FLOAT_GREAT_OR_EQUAL_AND_NOT_NAN, left, right, "fge");
                else
                    yield builder.compareInt(Comparator.SIGNED_INTEGER_GREATER_OR_EQUAL, left, right, "gte");
            }

            case LESS_THAN -> {
                if (resultType.isFloating())
                    yield builder.compareFloat(Comparator.FLOAT_LESS_THAN_AND_NOT_NAN, left, right, "flt");
                else
                    yield builder.compareInt(Comparator.SIGNED_INTEGER_LESS_THAN, left, right, "lt");
            }

            case LESS_OR_EQUAL -> {
                if (resultType.isFloating())
                    yield builder.compareFloat(Comparator.FLOAT_LESS_OR_EQUAL_AND_NOT_NAN, left, right, "fle");
                else
                    yield builder.compareInt(Comparator.SIGNED_INTEGER_LESS_OR_EQUAL, left, right, "lte");
            }

            case REMAINDER -> {
                if (resultType.isFloating())
                    yield builder.remainderFloat(left, right, "frem");
                else
                    yield builder.signedRemainder(left, right, "rem");
            }

            case AND -> builder.and(left, right, "and");
            case OR -> builder.or(left, right, "or");

            default -> throw new IllegalStateException(
                "Unable to generate complex operator for " + operator + " of types LHS: "
                + leftType + " RHS: " + rightType);
        };
    }

    /**
     * Get the wrapped type of this value.
     * @return wrapped value type
     */
    @Override
    public Type getValueType() {
        return resultType.toType();
    }

    @Override
    public String toString() {
        return left + " " + operator.getValue() + " " + right;
    }
}
