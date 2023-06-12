package org.voidlang.compiler.node.info;

import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeType;
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
     * @param pkg node package
     * @param name package name
     */
    public PackageImport(Package pkg, String name) {
        super(NodeType.IMPORT, pkg);
        this.name = name;
    }

    /**
     * Print the string representation of this node.
     */
    @Override
    public void debug() {
        System.out.println("import \"" + name + '"');
    }

    /**
     * Generate an LLVM instruction for this node
     * @return node ir code wrapper
     */
    @Override
    public Value generate() {
        throw new IllegalStateException("Cannot generate IR code for " + PackageImport.class);
    }

    /**
     * Get the name of the package to be imported.
     */
    public String getName() {
        return name;
    }
}
