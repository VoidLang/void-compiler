package org.voidlang.compiler.node;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.voidlang.compiler.node.common.Error;
import org.voidlang.compiler.node.common.Finish;
import org.voidlang.llvm.element.Builder;
import org.voidlang.llvm.element.Value;

/**
 * Represents an instruction node that is parsed from raw tokens.
 * A node can be an exact instruction or a holder of multiple instructions.
 * The node hierarchy is then transformed to executable bytecode.
 */
@Getter
@AllArgsConstructor
public abstract class Node {
    protected static final Prettier prettier = new Prettier();

    /**
     * The type of the node.
     */
    private final NodeType nodeType;

    /**
     * The target package of the node.
     */
    private final Package pkg;

    /**
     * Indicate, whether this node has the given type.
     * @param type target type to check
     * @return true if the type matches this node
     */
    public boolean is(NodeType type) {
        return this.nodeType == type;
    }

    /**
     * Indicate, whether this token is not a finish token.
     * @return true if there are more tokens to be parsed
     */
    public boolean hasNext() {
        return !(this instanceof Error)
            && !(this instanceof Finish);
    }

    /**
     * Print the string representation of this node.
     */
    public final void debug() {
        prettier.begin(this);
        prettier.content(this);
        prettier.end();
    }

    /**
     * Generate an LLVM instruction for this node
     * @param builder instruction builder for the current context
     * @return node ir code wrapper
     */
    public abstract Value generate(Builder builder);
}
