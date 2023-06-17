package org.voidlang.compiler.node.info;

import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.llvm.element.IRValue;

/**
 * Represents an importation of a package.
 */
public class PackageImport extends Node {
    /**
     * The name of the package to be imported.
     */
    private final String name;

    /**
     * Initialize the node.
     * @param name package name
     */
    public PackageImport(String name) {
        super(NodeType.IMPORT);
        this.name = name;
    }

    /**
     * Get the name of the package to be imported.
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
