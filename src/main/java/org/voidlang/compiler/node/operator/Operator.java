package org.voidlang.compiler.node.operator;

import dev.inventex.octa.console.ConsoleFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * Represents an enum of defined operators in the Abstract Syntax Tree.
 * The priority of the operator is defined by {@link #getPrecedence()}, which will then
 * be used to transform the operation tree according to the precedence of each operator.
 */
@AllArgsConstructor
@Getter
public enum Operator {
    AND("&&", 0, 0),
    OR("||", 0, 0),

    ADD("+", 1, 0),
    ADD_EQUAL("+=", 1, 0),
    INCREMENT("++", 1, 0),

    SUBTRACT("-", 1, 0),
    SUBTRACT_EQUAL("-=", 1, 0),
    DECREMENT("--", 1, 0),

    MULTIPLY("*", 2, 0),
    MULTIPLY_EQUAL("*=", 2, 0),

    DIVIDE("/", 2, 0),
    DIVIDE_EQUAL("/=", 2, 0),

    REMAINDER("%", 2, 0),
    REMAINDER_EQUAL("%=", 2, 0),

    POWER("^", 3, 1),
    POWER_EQUAL("^=", 3, 1),

    EQUAL("==", 4, 0),
    NOT_EQUAL("!=", 4, 0),
    GREATER_THAN(">", 4, 0),
    GREATER_OR_EQUAL(">=", 4, 0),
    LESS_THAN("<", 4, 0),
    LESS_OR_EQUAL("<=", 4, 0),

    SLICE(":", 5, 0),
    LAMBDA("::", 5, 0),

    ARROW("->", 6, 0),

    ASSIGN("=", 7, 0),

    UNKNOWN("<unk>", -1, -1);

    /**
     * The name of the operator.
     */
    private final String value;

    /**
     * The priority of the operator in the operation tree. The higher this value is,
     * the sooner the operation will be executed.
     */
    private final int precedence;

    /**
     * TODO
     */
    private final int associativity;

    /**
     * Find the wrapper for the given operator value.
     * @param value raw operator value
     * @return operator wrapper
     */
    public static Operator of(String value) {
        return Arrays.stream(values())
            .filter(operator -> operator.value.equals(value))
            .findFirst()
            .orElse(UNKNOWN);
    }

    @Override
    public String toString() {
        return ConsoleFormat.YELLOW + name();
    }
}
