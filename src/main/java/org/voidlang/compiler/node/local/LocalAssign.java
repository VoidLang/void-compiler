package org.voidlang.compiler.node.local;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeInfo;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.compiler.node.control.Element;
import org.voidlang.compiler.node.method.MethodCall;
import org.voidlang.compiler.node.type.core.Type;
import org.voidlang.compiler.node.value.Value;
import org.voidlang.llvm.element.IRBuilder;
import org.voidlang.llvm.element.IRType;
import org.voidlang.llvm.element.IRValue;

@RequiredArgsConstructor
@Getter
@NodeInfo(type = NodeType.LOCAL_ASSIGN)
public class LocalAssign extends Value {
    private final String name;

    private final Value value;

    private PointerOwner owner;

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
        owner = (PointerOwner) resolveName(name);

        if (!(owner instanceof Mutable))
            throw new IllegalStateException("Unable to assign to immutable variable `" + name + "`.");
    }

    /**
     * Initialize all class member declarations for the overriding node.
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessMember(Generator generator) {
        if (value != null)
            value.postProcessMember(generator);
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

        IRValue pointer = owner.getPointer();
        IRType pointerType = owner.getPointerType();

        /*
        // let the value allocate the value if it is an allocator
        // this happens when using the "new" keyword
        if (value instanceof Allocator allocator) {
            pointerType = allocator.getPointerType();
            pointer = allocator.allocate(generator, name);
        }
        // let the method call allocate the value for method calls
        else if (value instanceof MethodCall call && call.getMethod().getResolvedType() instanceof PassedByReference ref) {
            pointerType = ((Element) ref).getPointerType();
            pointer = call.generateNamed(generator, name);
        }
         */
        // assign the value to the already allocated stack
        // else {
            IRValue value = getValue().generate(generator);
            builder.store(value, pointer);
        // }

        // TODO maybe load the value from allocated pointer
        //  for now, that should be done manually by a Node
        return null;
        // return builder.load(pointerType, pointer, "");
    }

    /**
     * Get the wrapped type of this value.
     * @return wrapped value type
     */
    @Override
    public Type getValueType() {
        return owner.getValueType();
    }
}
