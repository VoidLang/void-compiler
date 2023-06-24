package org.voidlang.compiler.node.value;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeInfo;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.compiler.node.local.Allocator;
import org.voidlang.compiler.node.local.PointerOwner;
import org.voidlang.compiler.node.type.QualifiedName;
import org.voidlang.compiler.node.type.core.Type;
import org.voidlang.llvm.element.IRBuilder;
import org.voidlang.llvm.element.IRContext;
import org.voidlang.llvm.element.IRType;
import org.voidlang.llvm.element.IRValue;

import java.util.List;

@RequiredArgsConstructor
@Getter
@NodeInfo(type = NodeType.NEW)
public class New extends Value implements PointerOwner, Allocator {
    private final QualifiedName name;

    private final List<Value> arguments;

    private final Initializator initializator;

    private Type type;

    private IRType pointerType;
    private IRValue pointer;

    /**
     * Generate an LLVM instruction for this node
     * @param generator LLVM instruction generation context
     */
    @Override
    public IRValue generate(Generator generator) {
        // create an unnamed allocation if the result is not meant to be handled.
        // e.g. if the constructor is called in a method call
        // greet(new Person("John"))
        //       ^^^^^^ here is an anonymous value of Person, which isn't meant to be mutated
        return allocate(generator, "");
    }

    @Override
    public IRValue allocate(Generator generator, String name) {
        IRContext context = generator.getContext();
        IRBuilder builder = generator.getBuilder();

        pointerType = getType().generateType(context);
        return pointer = builder.alloc(pointerType, name);
    }

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
     * Initialize all type uses for the overriding node.
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessUse(Generator generator) {
        type = resolveType(name.getDirect());
        if (type == null)
            throw new IllegalStateException("Unable to fetch type for New: " + name.getDirect());
    }

    @Override
    public IRValue getPointer() {
        return pointer;
    }

    @Override
    public IRType getPointerType() {
        return pointerType;
    }

    /**
     * Get the wrapped type of this value.
     * @return wrapped value type
     */
    @Override
    public Type getValueType() {
        return type;
    }
}
