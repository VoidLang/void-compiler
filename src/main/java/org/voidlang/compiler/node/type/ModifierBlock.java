package org.voidlang.compiler.node.type;

import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.compiler.node.common.Finish;
import org.voidlang.llvm.element.Value;

import java.util.List;

/**
 * Represents a block of modifiers that are applied for the following type nodes in the scope.
 */
public class ModifierBlock extends Node {
    /**
     * The list of the access modifiers in this block.
     */
    private final List<String> modifiers;

    /**
     * Initialize the node.
     * @param pkg node package
     */
    public ModifierBlock(Package pkg, List<String> modifiers) {
        super(NodeType.MODIFIER_BLOCK, pkg);
        this.modifiers = modifiers;
    }

    /**
     * Print the string representation of this node.
     */
    @Override
    public void debug() {
        System.out.println(String.join(" ", modifiers) + ':');
    }

    /**
     * Generate an LLVM instruction for this node
     *
     * @return node ir code wrapper
     */
    @Override
    public Value generate() {
        throw new IllegalStateException("Cannot generate IR code for " + ModifierBlock.class);
    }

    /**
     * Get the list of the access modifiers in this block.
     */
    public List<String> getModifiers() {
        return modifiers;
    }
}
