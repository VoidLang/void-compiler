package org.voidlang.compiler.node.info;

import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.llvm.element.IRValue;

/**
 * Represents a package name declaration of a file.
 */
public class PackageSet extends Node {
    /**
     * The name of the package to be declared.
     */
    private final String name;

    /**
     * Initialize the node.
     * @param name package name
     */
    public PackageSet(String name) {
        super(NodeType.PACKAGE);
        this.name = name;
    }

    /**
     * Get the name of the package to be declared.
     */
    public String getName() {
        return name;
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
