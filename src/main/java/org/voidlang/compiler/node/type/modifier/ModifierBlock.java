package org.voidlang.compiler.node.type.modifier;

import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.llvm.element.IRBuilder;
import org.voidlang.llvm.element.IRValue;

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
     */
    public ModifierBlock(List<String> modifiers) {
        super(NodeType.MODIFIER_BLOCK);
        this.modifiers = modifiers;
    }

    /**
     * Get the list of the access modifiers in this block.
     */
    public List<String> getModifiers() {
        return modifiers;
    }

    /**
     * Generate an LLVM instruction for this node
     * @param generator LLVM instruction generation context
     */
    @Override
    public IRValue generate(Generator generator) {
        return null;
    }
}
