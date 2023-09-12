package org.voidlang.compiler.node.element;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.voidlang.compiler.node.*;
import org.voidlang.compiler.node.local.LazyPointerOwner;
import org.voidlang.compiler.node.local.LocalDeclareAssign;
import org.voidlang.compiler.node.local.PassedByReference;
import org.voidlang.compiler.node.local.PointerOwner;
import org.voidlang.compiler.node.type.QualifiedName;
import org.voidlang.compiler.node.type.core.ScalarType;
import org.voidlang.compiler.node.type.core.Type;
import org.voidlang.compiler.node.type.name.Name;
import org.voidlang.compiler.node.type.name.ScalarName;
import org.voidlang.compiler.node.type.named.MethodParameter;
import org.voidlang.compiler.node.type.named.NamedScalarType;
import org.voidlang.compiler.node.value.Value;
import org.voidlang.llvm.element.*;

import java.util.*;

@RequiredArgsConstructor
@Getter
@NodeInfo(type = NodeType.METHOD)
public class Method extends Node {

    private final Type returnType;

    private final String name;

    private final List<MethodParameter> parameters;

    private final List<Node> body;

    private Type resolvedType;
    private IRFunction function;
    private List<IRType> paramTypes;
    private Generator generator;

    private final Map<String, ParameterIndexer> paramCache = new HashMap<>();

    @Override
    public void preProcess(Node parent) {
        this.parent = parent;
        for (Node node : body) {
            node.preProcess(this);
            if (node instanceof Instruction instruction)
                instruction.setContext(this);
        }
    }

    /**
     * Initialize all class member declarations for the overriding node.
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessMember(Generator generator) {
        for (Node node : body)
            node.postProcessMember(generator);

        // extract the context from the generator
        IRContext context = generator.getContext();
        IRModule module = generator.getModule();

        // create the signature of the LLVM function
        resolvedType = getReturnType();
        if (resolvedType instanceof NamedScalarType scalar) {
            QualifiedName name = ((ScalarType) scalar.getScalarType()).getName();
            if (!name.isPrimitive())
                resolvedType = resolveType(name.getDirect());
        }

        if (resolvedType == null)
            throw new IllegalStateException("Unable to resolve method return type " + returnType);

        IRType returnType = resolvedType.generateType(context);

        // make sure to return a pointer for class types
        if (resolvedType instanceof PassedByReference)
            returnType = returnType.toPointerType();

        paramTypes = parameters
            .stream()
            .map(p -> {
                IRType type = p.getType().generateType(context);
                if (type == null)
                    throw new IllegalStateException(p.getType() + " does not have a generator");
                return type;
            })
            .toList();
        IRFunctionType functionType = IRFunctionType.create(returnType, paramTypes);

        // create the LLVM function for the target context
        function = IRFunction.create(module, name, functionType);
    }

    /**
     * Initialize all type declarations for the overriding node.
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessType(Generator generator) {
        for (Node node : body)
            node.postProcessType(generator);
    }

    /**
     * Initialize all type uses for the overriding node.
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessUse(Generator generator) {
        for (Node node : body)
            node.postProcessUse(generator);
    }

    /**
     * Generate an LLVM instruction for this node
     * @param generator LLVM instruction generation context
     */
    @Override
    public IRValue generate(Generator generator) {
        // extract the context from the generator
        IRContext context = generator.getContext();
        IRBuilder builder = generator.getBuilder();
        this.generator = generator;

        // create an entry block for the function
        IRBlock block = IRBlock.create(context, function, "entry");
        builder.positionAtEnd(block);

        // generate the LLVM instructions for the body of the function
        for (Node node : body)
            node.generate(generator);

        return function;
    }

    public boolean checkTypes(List<Type> types) {
        System.err.println("check param types");

        if (types.size() != parameters.size())
            return false;

        for (int i = 0; i < types.size(); i++) {
            Type checkType = types.get(i);
            Type paramType = parameters.get(i).getType();

            if (paramType instanceof ScalarType scalar && !scalar.getName().isPrimitive()) {
                paramType = resolveType(scalar.getName().getDirect());
            }

            System.err.println("check " + checkType + " " + checkType.getClass());
            System.err.println("param " + paramType + " " + paramType.getClass());

            if (!checkType.equals(paramType))
                return false;
        }

        return true;
    }

    public IRFunction getFunction() {
        return function;
    }


    @Override
    public Value resolveName(String name) {
        // resolve method parameters
        for (int i = 0; i < parameters.size(); i++) {
            MethodParameter parameter = parameters.get(i);
            Name paramName = parameter.getName();
            if (paramName.isScalar()) {
                String value = ((ScalarName) paramName).getValue();
                if (!value.equals(name))
                    continue;
                final int index = i;
                return paramCache.computeIfAbsent(name, k -> new ParameterIndexer(index, parameter.getType()));
            }
        }
        // resolve local variables
        for (Node node : body) {
            if (node instanceof LocalDeclareAssign local) {
                if (!local.getName().equals(name))
                    continue;
                return (Value) node;
            }
        }
        return super.resolveName(name);
    }

    @RequiredArgsConstructor
    @NodeInfo(type = NodeType.PARAMETER_INDEXER)
    private class ParameterIndexer extends Value implements LazyPointerOwner {
        private final int index;

        private final Type type;

        private IRType pointerType;
        private IRValue pointer;

        /**
         * Initialize all the child nodes for the overriding node.
         * @param parent parent node of the overriding node
         */
        @Override
        public void preProcess(Node parent) {
            this.parent = parent;
            for (Node node : body)
                node.preProcess(this);
        }

        /**
         * Initialize all type declarations for the overriding node.
         * @param generator LLVM code generator
         */
        @Override
        public void postProcessType(Generator generator) {
            for (Node node : body)
                node.postProcessType(generator);
        }

        /**
         * Initialize all class member declarations for the overriding node.
         * @param generator LLVM code generator
         */
        @Override
        public void postProcessMember(Generator generator) {
            for (Node node : body)
                node.postProcessMember(generator);
        }

        /**
         * Initialize all type uses for the overriding node.
         * @param generator LLVM code generator
         */
        @Override
        public void postProcessUse(Generator generator) {
            for (Node node : body)
                node.postProcessUse(generator);
        }

        /**
         * Generate an LLVM instruction for this node
         * @param generator LLVM instruction generation context
         */
        @Override
        public IRValue generate(Generator generator) {
            // use the allocated pointer instead if the modifier was mutated
            if (pointer != null)
                return generator.getBuilder().load(pointerType, pointer, String.valueOf(index));
            return function.getParameter(index);
        }

        @Override
        public IRValue getPointer() {
            if (pointer == null) {
                // lazy allocate the pointer on the stack, as by default
                // we are not expecting the method parameters to be written to,
                // but only to be read from. therefore if we want to edit the parameter
                // we must lazily allocate it on the stack, and assign it the parameter value
                pointerType = type.generateType(generator.getContext());
                IRBuilder builder = generator.getBuilder();
                // lazy allocate a pointer of the modifiable parameter
                pointer = builder.alloc(pointerType, String.valueOf(index));
                // store the original parameter value in the modifiable parameter
                builder.store(generate(generator), pointer);
            }
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
}
