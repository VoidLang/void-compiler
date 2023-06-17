package org.voidlang.compiler.node.type.modifier;

import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.llvm.element.IRValue;

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
     * The list of the access modifiers.
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
