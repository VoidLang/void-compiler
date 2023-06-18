package org.voidlang.compiler.node.method;

import lombok.Getter;
import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.compiler.node.type.QualifiedName;
import org.voidlang.llvm.element.IRBuilder;
import org.voidlang.llvm.element.IRContext;
import org.voidlang.llvm.element.IRModule;
import org.voidlang.llvm.element.IRValue;

import java.util.List;

@Getter
public class MethodCall extends Node {
    private final QualifiedName name;
    private final List<Node> arguments;

    public MethodCall(QualifiedName name, List<Node> arguments) {
        super(NodeType.METHOD_CALL);
        this.name = name;
        this.arguments = arguments;
    }

    /**
     * Generate an LLVM instruction for this node
     *
     * @param generator LLVM instruction generation context
     */
    @Override
    public IRValue generate(Generator generator) {
        // extract the context from the generator
        IRContext context = generator.getContext();
        IRModule module = generator.getModule();
        IRBuilder builder = generator.getBuilder();

        return null;
    }
}
