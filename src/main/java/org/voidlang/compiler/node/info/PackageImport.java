package org.voidlang.compiler.node.info;

import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.llvm.element.Builder;
import org.voidlang.llvm.element.Value;

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
     * Generate an LLVM instruction for this node
     * @return node ir code wrapper
     */
    @Override
    public Value generate(Builder builder) {
        throw new IllegalStateException("Cannot generate IR code for " + PackageImport.class);
    }

    /**
     * Get the name of the package to be imported.
     */
    public String getName() {
        return name;
    }
}
