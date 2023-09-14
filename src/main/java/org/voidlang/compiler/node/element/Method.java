package org.voidlang.compiler.node.element;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.voidlang.compiler.node.*;
import org.voidlang.compiler.node.control.Return;
import org.voidlang.compiler.node.local.*;
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
import java.util.stream.Collectors;

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

    private String finalName;

    private final Map<String, Value> paramCache = new HashMap<>();

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

        // generate IR types for the method parameters
        paramTypes = parameters
            .stream()
            .map(p -> {
                IRType type = p.getType().generateType(context);
                if (type == null)
                    throw new IllegalStateException(p.getType() + " does not have a generator");
                return type;
            })
            .collect(Collectors.toCollection(ArrayList::new));

        // TODO ignore static
        // add the parent element as 'this' for the first argument
        if (parent instanceof Class clazz)
            paramTypes.add(0, IRType.pointerType(clazz.generateType(context)));

        IRFunctionType functionType = IRFunctionType.create(returnType, paramTypes);

        finalName = name;
        if (parent instanceof Class clazz)
            finalName = clazz.getName() + "::" + finalName;

        // create the LLVM function for the target context
        function = IRFunction.create(module, finalName, functionType);
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

        if (body.isEmpty() || body.get(body.size() - 1).getNodeType() != NodeType.RETURN)
            body.add(new Return(null));

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

            // TODO: resolve method param types on preprocess
            if (paramType instanceof ScalarType scalar && !scalar.getName().isPrimitive()) {
                paramType = resolveType(scalar.getName().getDirect());
                if (paramType == null)
                    throw new IllegalStateException("Unable to resolve method parameter type: " + scalar.getName());
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
                return paramCache.computeIfAbsent(name, k -> {
                    if (parameter.isMutable())
                        return new MutableParameterIndexer(index, parameter.getType());
                    else
                        return new ImmutableParameterIndexer(index, parameter.getType());
                });
            }
        }
        // resolve local variables
        for (Node node : body) {
            if (node instanceof ImmutableLocalDeclareAssign local) {
                if (!local.getName().equals(name))
                    continue;
                return local;
            } else if (node instanceof MutableLocalDeclareAssign local) {
                if (!local.getName().equals(name))
                    continue;
                return local;
            }
        }
        return super.resolveName(name);
    }

    /**
     * Immutable method parameters don't expect to be mutated. Therefore, loading the value of the method parameter
     * will purely load the LLVM parameter. As this is not allocated manually, the value cannot be assigned.
     */
    @RequiredArgsConstructor
    @NodeInfo(type = NodeType.IMMUTABLE_PARAMETER_INDEXER)
    private class ImmutableParameterIndexer extends Value implements PointerOwner {
        private final int index;

        private final Type type;

        private IRValue pointer;
        private IRType pointerType;

        /**
         * Generate an LLVM instruction for this node
         * @param generator LLVM instruction generation context
         */
        @Override
        public IRValue generate(Generator generator) {
            return function.getParameter(index);
        }

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

        @Override
        public IRValue getPointer() {
            if (pointer == null) {
                pointer = function.getParameter(index);
                pointerType = type.generateType(generator.getContext());
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

        @Override
        public IRValue load(Generator generator) {
            return generate(generator);
        }
    }

    /**
     * Mutable method parameters expect the value to be mutated. Therefore, the type of the parameter is allocated
     * on the stack. Accessing the method parameter loads the value from the allocated value on the stack.
     * Assigning the method parameter stores the value that is allocated on the stack.
     */
    @RequiredArgsConstructor
    @NodeInfo(type = NodeType.MUTABLE_PARAMETER_INDEXER)
    private class MutableParameterIndexer extends Value implements PointerOwner, Mutable {
        private final int index;

        private final Type type;

        private IRValue pointer;
        private IRType pointerType;

        private boolean allocated;

        /**
         * Generate an LLVM instruction for this node
         * @param generator LLVM instruction generation context
         */
        @Override
        public IRValue generate(Generator generator) {
            IRBuilder builder = generator.getBuilder();

            if (!allocated) {
                getPointer();
                allocated = true;
            }

            return builder.load(pointerType, pointer, "load param " + index);
        }

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

        @Override
        public IRValue getPointer() {
            IRBuilder builder = generator.getBuilder();
            if (pointer == null) {
                // allocate the pointer on the stack, as we are expecting the method parameters to be written to,
                // therefore if we want to edit the parameter
                // we must allocate it on the stack, and assign it the parameter value
                pointerType = type.generateType(generator.getContext());
                // lazy allocate a pointer of the modifiable parameter
                pointer = builder.alloc(pointerType, "mut param " + index);
                // store the original parameter value in the modifiable parameter
                builder.store(function.getParameter(index), pointer);
            }
            return pointer;
        }

        @Override
        public IRType getPointerType() {
            return pointerType;
        }

        /**
         * Get the wrapped type of this value.
         *
         * @return wrapped value type
         */
        @Override
        public Type getValueType() {
            return type;
        }

        @Override
        public IRValue load(Generator generator) {
            return generate(generator);
        }
    }
}
