package org.voidlang.compiler.node.type;

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
     * @param pkg node package
     */
    public Modifiable(NodeType type, Package pkg, List<String> modifiers) {
        super(type, pkg);
        this.modifiers = modifiers;
    }

    /**
     * Initialize the node.
     * @param type node type
     * @param pkg node package
     */
    public Modifiable(NodeType type, Package pkg) {
        super(type, pkg);
        modifiers = new ArrayList<>();
    }

    /**
     * Get the list of the access modifiers of this target.
     */
    public List<String> getModifiers() {
        return modifiers;
    }
}
