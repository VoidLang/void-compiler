package org.voidlang.compiler.node.type.modifier;

import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeType;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a node in the Abstract Syntax Tree, that is capable of having access modifiers.
 */
public abstract class Modifiable extends Node {
    /**
     * The list of the access modifiers of this target.
     */
    private final List<String> modifiers;

    /**
     * Initialize the node.
     * @param type node type
     */
    public Modifiable(NodeType type, List<String> modifiers) {
        super(type);
        this.modifiers = modifiers;
    }

    /**
     * Initialize the node.
     * @param type node type
     */
    public Modifiable(NodeType type) {
        super(type);
        modifiers = new ArrayList<>();
    }

    /**
     * Get the list of the access modifiers of this target.
     */
    public List<String> getModifiers() {
        return modifiers;
    }
}
