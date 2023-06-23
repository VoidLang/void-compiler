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
            default -> null;
        };
    }


    /**
     * Get the wrapped type of this value.
     * @return wrapped value type
     */
    @Override
    public Type getValueType() {
        return switch (getValue().getType()) {
            case BYTE -> Type.BYTE;
            case SHORT -> Type.SHORT;
            case INTEGER -> Type.INT;
            case LONG -> Type.LONG;
            default -> null;
        };
    }
}
