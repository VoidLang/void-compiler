package org.voidlang.compiler.node.local;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeInfo;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.compiler.node.element.Class;
import org.voidlang.compiler.node.type.core.Type;
import org.voidlang.compiler.node.value.Value;
import org.voidlang.llvm.element.IRBuilder;
import org.voidlang.llvm.element.IRContext;
import org.voidlang.llvm.element.IRType;
import org.voidlang.llvm.element.IRValue;

@RequiredArgsConstructor
@Getter
@NodeInfo(type = NodeType.LOCAL_DECLARE_ASSIGN)
public class LocalDeclareAssign extends Value implements PointerOwner, Loadable {
    private final Type type;

    private final String name;

    private final Value value;

    private IRType pointerType;
    private IRValue pointer;

    /**
     * Initialize all the child nodes for the overriding node.
     * @param parent parent node of the overriding node
     */
    @Override
    public void preProcess(Node parent) {
        this.parent = parent;
        if (value != null)
            value.preProcess(this);
    }

    /**
     * Initialize all type declarations for the overriding node.
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessType(Generator generator) {
        if (value != null)
            value.postProcessType(generator);
    }

    /**
     * Initialize all type uses for the overriding node.
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessUse(Generator generator) {
        if (value != null)
            value.postProcessUse(generator);
    }

    /**
     * Generate an LLVM instruction for this node
     * @param generator LLVM instruction generation context
     */
    @Override
    public IRValue generate(Generator generator) {
        IRBuilder builder = generator.getBuilder();
        IRContext context = builder.getContext();

        pointerType = getValue().getValueType().generateType(context);

        // let the value allocate the value if it is an allocator
        if (value instanceof Allocator allocator)
            pointer = allocator.allocate(generator, name);
        // allocate the value on the stack, and assign its value
        else {
            pointer = builder.alloc(pointerType, name);

            IRValue value = getValue().generate(generator);
            builder.store(value, pointer);
        }

        // do not load the values from class struct pointers, as classes
        // are meant to be handled by reference, and not by value
        if (value.getValueType() instanceof Class)
            return pointer;
        // TODO maybe load the value from allocated pointer
        //  for now, that should be done manually by a Node
        return null;
    }

    @Override
    public IRValue load(Generator generator) {
        return generator.getBuilder().load(pointerType, pointer, name);
    }

    /**
     * Get the wrapped type of this value.
     *
     * @return wrapped value type
     */
    @Override
    public Type getValueType() {
        return value.getValueType();
    }
}
