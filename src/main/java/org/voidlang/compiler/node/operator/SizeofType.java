package org.voidlang.compiler.node.operator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeInfo;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.compiler.node.local.Loadable;
import org.voidlang.compiler.node.type.core.Type;
import org.voidlang.compiler.node.type.named.NamedScalarType;
import org.voidlang.compiler.node.value.Value;
import org.voidlang.llvm.element.IRType;
import org.voidlang.llvm.element.IRValue;

@NodeInfo(type = NodeType.SIZEOF_TYPE)
@RequiredArgsConstructor
@Getter
public class SizeofType extends Value implements Loadable {
    private final Type type;

    private IRValue size;

    /**
     * Generate an LLVM instruction for this node
     *
     * @param generator LLVM instruction generation context
     */
    @Override
    public IRValue generate(Generator generator) {
        if (size != null)
            return size;

        Type type = getType();
        if (type instanceof NamedScalarType named)
            type = named.getScalarType();

        IRType irType = type.generateType(generator.getContext());
        return size = irType.size();
    }

    @Override
    public IRValue load(Generator generator) {
        return generate(generator);
    }

    /**
     * Initialize all the child nodes for the overriding node.
     *
     * @param parent parent node of the overriding node
     */
    @Override
    public void preProcess(Node parent) {
        this.parent = parent;
    }

    /**
     * Initialize all type declarations for the overriding node.
     *
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessType(Generator generator) {
    }

    /**
     * Initialize all class member declarations for the overriding node.
     *
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessMember(Generator generator) {
    }

    /**
     * Initialize all type uses for the overriding node.
     *
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessUse(Generator generator) {
    }


    /**
     * Get the wrapped type of this value.
     *
     * @return wrapped value type
     */
    @Override
    public Type getValueType() {
        // TODO make it an unsigned long
        return Type.LONG;
    }
}
