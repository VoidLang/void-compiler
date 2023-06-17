package org.voidlang.compiler.node.element;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.compiler.node.type.core.Type;
import org.voidlang.compiler.node.type.named.MethodParameter;
import org.voidlang.llvm.element.Builder;
import org.voidlang.llvm.element.Value;

import java.util.List;

@Getter
public class Method extends Node {
    private final Type returnType;

    private final String name;

    private final List<MethodParameter> parameters;

    private final List<Node> body;

    public Method(Package pkg, Type returnType, String name, List<MethodParameter> parameters, List<Node> body) {
        super(NodeType.METHOD, pkg);
        this.returnType = returnType;
        this.name = name;
        this.parameters = parameters;
        this.body = body;
    }

    /**
     * Generate an LLVM instruction for this node
     *
     * @param builder instruction builder for the current context
     * @return node ir code wrapper
     */
    @Override
    public Value generate(Builder builder) {
        return null;
    }
}
