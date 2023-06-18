package org.voidlang.compiler.node.type.modifier;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeInfo;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.llvm.element.IRValue;

import java.util.List;

/**
 * Represents a declaration of modifiers that are yet to be assigned for the following type node.
 */
@RequiredArgsConstructor
@Getter
@NodeInfo(type = NodeType.MODIFIER_LIST)
public class ModifierList extends Node {
    /**
     * The list of the access modifiers.
     */
    private final List<String> modifiers;

    /**
     * Generate an LLVM instruction for this node
     * @param generator LLVM instruction generation context
     */
    @Override
    public IRValue generate(Generator generator) {
        return null;
    }
}
