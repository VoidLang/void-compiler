package org.voidlang.compiler.node.local;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeInfo;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.compiler.node.element.Class;
import org.voidlang.compiler.node.memory.HeapAllocator;
import org.voidlang.compiler.node.memory.StackAllocator;
import org.voidlang.compiler.node.method.MethodCall;
import org.voidlang.compiler.node.type.QualifiedName;
import org.voidlang.compiler.node.type.core.ScalarType;
import org.voidlang.compiler.node.type.core.Type;
import org.voidlang.compiler.node.type.named.NamedScalarType;
import org.voidlang.compiler.node.array.ArrayAllocate;
import org.voidlang.compiler.node.value.New;
import org.voidlang.compiler.node.value.Value;
import org.voidlang.llvm.element.IRBuilder;
import org.voidlang.llvm.element.IRContext;
import org.voidlang.llvm.element.IRType;
import org.voidlang.llvm.element.IRValue;

@RequiredArgsConstructor
@Getter
@NodeInfo(type = NodeType.MUTABLE_LOCAL_DECLARE_ASSIGN)
public class MutableLocalDeclareAssign extends Value implements PointerOwner, Loadable, Mutable {
    private final Type type;

    private final String name;

    private final Value value;

    private IRType pointerType;
    private IRValue pointer;

    private Type resolvedType;

    private boolean loaded;

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
        IRContext context = builder.getContext();

        // do not reallocate if it was already allocated. the problem is that whenever
        //  this value is accessed, it is does reallocate the value, instead it should pass the value only
        if (loaded) {
            // do not load the values from class struct pointers or arrays, as classes
            // are meant to be handled by reference, and not by value
            if (value.getValueType() instanceof Class ||
                    (value.getValueType() instanceof ScalarType scala && !scala.getArray().getDimensions().isEmpty()))
                return pointer;

            // local variable is meant to be passed by value, so load it from the pointer
            return load(generator);
        }

        pointerType = getType().generateType(context);

        // the case of the `new` keyword is special, as it implements both StackAllocator and HeapAllocator
        // let the keyboard decide whether to allocate on the stack or on the heap
        if (value instanceof New)
            pointer = value.generateNamed(generator, "let (new) " + name);

        // let the value allocate the value on the stack
        // this happens when using the "new" keyword
        else if (value instanceof StackAllocator allocator)
            pointer = allocator.allocateStack(generator, "mut (alloc) " + name);

        // let the value allocate the value on the heap
        else if (value instanceof HeapAllocator allocator)
            pointer = allocator.allocateHeap(generator, "mut (malloc) " + name);

        // let the method call allocate the value for method calls
        else if (value instanceof MethodCall call && call.getMethod().getResolvedType() instanceof PassedByReference)
            pointer = call.generateNamed(generator, "mut (method) " + name);

        // handle array allocation
        else if (value instanceof ArrayAllocate array)
            pointer = array.generate(generator);

        // allocate the value on the stack, and assign its value
        else {
            pointer = builder.alloc(pointerType, "mut (ptr) " + name);

            IRValue value = getValue().generate(generator);
            builder.store(value, pointer);
        }

        loaded = true;

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
     * @return wrapped value type
     */
    @Override
    public Type getValueType() {
        return value.getValueType();
    }

    public Type getType() {
        if (resolvedType != null)
            return resolvedType;

        resolvedType = getValueType();
        if (resolvedType instanceof NamedScalarType scalar) {
            QualifiedName name = ((ScalarType) scalar.getScalarType()).getName();
            if (!name.isPrimitive())
                resolvedType = resolveType(name.getDirect());
        }

        if (resolvedType == null)
            throw new IllegalStateException("Unable to resolve local variable value type " + getValueType());

        return resolvedType;
    }
}
