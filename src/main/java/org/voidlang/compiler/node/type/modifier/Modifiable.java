package org.voidlang.compiler.node.type.modifier;

import lombok.RequiredArgsConstructor;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeType;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a node in the Abstract Syntax Tree, that is capable of having access modifiers.
 */
@RequiredArgsConstructor
public abstract class Modifiable extends Node {
    /**
     * The list of the access modifiers of this target.
     */
    private final List<String> modifiers;

    /**
     * Get the list of the access modifiers of this target.
     */
    public List<String> getModifiers() {
        return modifiers;
    }
}
