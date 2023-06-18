package org.voidlang.compiler.node.info;

import lombok.RequiredArgsConstructor;
import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeInfo;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.llvm.element.IRValue;

/**
 * Represents an importation of a package.
 */
@RequiredArgsConstructor
@NodeInfo(type = NodeType.IMPORT)
public class PackageImport extends Node {
    /**
     * The name of the package to be imported.
     */
    private final String name;

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
