package org.voidlang.compiler.node.value;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.voidlang.compiler.node.*;
import org.voidlang.compiler.node.type.core.CompoundType;
import org.voidlang.compiler.node.type.core.Type;
import org.voidlang.compiler.node.type.pointer.Referencing;
import org.voidlang.llvm.element.*;

import java.util.List;

@RequiredArgsConstructor
@Getter
@NodeInfo(type = NodeType.TUPLE)
public class Tuple extends Value {
    private final List<Value> members;

    private CompoundType compoundType;

    /**
     * Initialize all the child nodes for the overriding node.
     * @param parent parent node of the overriding node
     */
    @Override
    public void preProcess(Node parent) {
        this.parent = parent;
        for (Node node : members)
            node.preProcess(this);
    }

    /**
     * Initialize all type declarations for the overriding node.
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessType(Generator generator) {
        for (Node node : members)
            node.postProcessType(generator);
    }

    /**
     * Initialize all class member declarations for the overriding node.
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessMember(Generator generator) {
        for (Node node : members)
            node.postProcessMember(generator);
    }

    /**
     * Initialize all type uses for the overriding node.
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessUse(Generator generator) {
        for (Node node : members)
            node.postProcessUse(generator);
    }

    /**
     * Generate an LLVM instruction for this node
     *
     * @param generator LLVM instruction generation context
     */
    @Override
    public IRValue generate(Generator generator) {
        throw new IllegalStateException("Tuple should be generated with IRStruct");
    }

    public IRValue generateTuple(Generator generator, IRStruct struct) {
        IRBuilder builder = generator.getBuilder();

        IRValue tuple = builder.alloc(struct, "tuple");

        for (int i = 0; i < members.size(); i++) {
            IRValue member = members.get(i).generate(generator);
            builder.store(member, builder.structMemberPointer(struct, tuple, i, "tuple-member-" + i));
        }

        // return builder.load(struct, tuple, "tuple-val");
        return tuple;
    }

    /**
     * Get the wrapped type of this value.
     *
     * @return wrapped value type
     */
    @Override
    public Type getValueType() {
        if (compoundType != null)
            return compoundType;
        return compoundType = new CompoundType(
            Referencing.none(),
            members
                .stream()
                .map(Value::getValueType)
                .toList()
        );
    }
}
