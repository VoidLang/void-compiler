package org.voidlang.compiler.node.info;

import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.compiler.node.common.Finish;
import org.voidlang.llvm.element.Builder;
import org.voidlang.llvm.element.Value;

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
     * @param pkg node package
     * @param name package name
     */
    public PackageSet(Package pkg, String name) {
        super(NodeType.PACKAGE, pkg);
        this.name = name;
    }

    /**
     * Generate an LLVM instruction for this node
     * @return node ir code wrapper
     */
    @Override
    public Value generate(Builder builder) {
        throw new IllegalStateException("Cannot generate IR code for " + Finish.class);
    }

    /**
     * Get the name of the package to be declared.
     */
    public String getName() {
        return name;
    }
}
