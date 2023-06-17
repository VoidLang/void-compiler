package org.voidlang.compiler.node.type.modifier;

import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.llvm.element.Builder;
import org.voidlang.llvm.element.Value;

import java.util.List;

/**
 * Represents a declaration of modifiers that are yet to be assigned for the following type node.
 */
public class ModifierList extends Node {
    /**
     * The list of the access modifiers.
     */
    private final List<String> modifiers;

    /**
     * Initialize the node.
     */
    public ModifierList(List<String> modifiers) {
        super(NodeType.MODIFIER_LIST);
        this.modifiers = modifiers;
    }

    /**
     * Generate an LLVM instruction for this node
     *
     * @return node ir code wrapper
     */
    @Override
    public Value generate(Builder builder) {
        throw new IllegalStateException("Cannot generate IR code for " + ModifierList.class);
    }

    /**
     * The list of the access modifiers.
     */
    public List<String> getModifiers() {
        return modifiers;
    }
}
