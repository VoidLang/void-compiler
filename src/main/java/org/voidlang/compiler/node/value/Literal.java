package org.voidlang.compiler.node.value;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeInfo;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.compiler.node.type.core.Type;
import org.voidlang.compiler.token.Token;
import org.voidlang.compiler.token.TokenType;
import org.voidlang.llvm.element.IRContext;
import org.voidlang.llvm.element.IRType;
import org.voidlang.llvm.element.IRValue;

@RequiredArgsConstructor
@Getter
@NodeInfo(type = NodeType.VALUE)
public class Literal extends Value {
    private final Token value;

    /**
     * Initialize all the child nodes for the overriding node.
     * @param parent parent node of the overriding node
     */
    @Override
    public void preProcess(Node parent) {
        this.parent = parent;
    }

    /**
     * Initialize all type declarations for the overriding node.
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessType(Generator generator) {
    }

    /**
     * Initialize all class member declarations for the overriding node.
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessMember(Generator generator) {
    }

    /**
     * Initialize all type uses for the overriding node.
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessUse(Generator generator) {
    }

    /**
     * Generate an LLVM instruction for this node
     * @param generator LLVM instruction generation context
     */
    @Override
    public IRValue generate(Generator generator) {
        TokenType type = getValue().getType();
        String value = getValue().getValue();

        IRContext context = generator.getContext();

        return switch (type) {
            case INTEGER -> IRType.int32(context).constInt(Integer.parseInt(value));
            case BOOLEAN -> IRType.int1(context).constInt("true".equals(value) ? 1 : 0);
            default -> throw new IllegalStateException("Unable to generate literal value for type " + type);
        };
    }


    /**
     * Get the wrapped type of this value.
     * @return wrapped value type
     */
    @Override
    public Type getValueType() {
        TokenType type = value.getType();
        return switch (type) {
            case BYTE -> Type.BYTE;
            case SHORT -> Type.SHORT;
            case INTEGER -> Type.INT;
            case LONG -> Type.LONG;
            case BOOLEAN -> Type.BOOL;
            default -> throw new IllegalStateException("Unable to get value type for literal " + type);
        };
    }

    @Override
    public String toString() {
        return value.getValue();
    }
}
