package org.voidlang.compiler.node.element;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import org.voidlang.compiler.node.*;
import org.voidlang.compiler.node.local.*;
import org.voidlang.compiler.node.method.FunctionContext;
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

/**
 * Represents a declared method in the application. A method can be directly put in a package,
 * or it can be declared in a parent structure.
 * <br>
 * If a method is declared in a structure and is non-static, the instance of the structure is passed as 'this'
 * for the first parameter.
 */
@RequiredArgsConstructor
@Getter
@NodeInfo(type = NodeType.METHOD)
public class Method extends Node {
    private static final Map<Method, String> methodNames = new HashMap<>();

    /**
     * The type of the method node in the AST.
     */
    private final Type returnType;

    /**
     * The declared name of the method.
     */
    private final String name;

    /**
     * The declared raw parameter types of the method node in the AST.
     */
    private final List<MethodParameter> parameters;

    /**
     * The declared body of the method node in the AST.
     */
    private final List<Node> body;

    /**
     * The resolved return type of the method, that is given by the parent context.
     */
    private Type resolvedType;

    /**
     * The generated LLVM method handle.
     */
    private IRFunction function;

    /**
     * The list of the generated LLVM parameter type handles.
     */
    private List<IRType> paramTypes;

    /**
     * The LLVM code emitter used for generating code.
     */
    private Generator generator;

    /**
     * The name of the method with a suffix, that makes it unique in the module.
     */
    private String uniqueName;

    /**
     * The final name that this method is registered as.
     */
    private String finalName;

    /**
     * Indicate, whether the LLVM representation of the method has been defined.
     */
    private boolean defined;

    /**
     * Indicate, whether this method has been declared without an explicit body.
     */
    @Setter
    private boolean bodyLess;

    /**
     * The map of the cached method resolvers of the method.
     * A resolver may be either an `ImmutableParameterIndexer` or a `MutableParameterIndexer`,
     * depending on whether a parameter has the `mut` modifier.
     */
    private final Map<String, Value> paramCache = new HashMap<>();

    /**
     * Initialize all the child nodes for the overriding node.
     * @param parent parent node of the overriding node
     */
    @Override
    public void preProcess(Node parent) {
        this.parent = parent;
        for (Node node : body) {
            if (node instanceof FunctionContext context)
                context.setContext(this);
            node.preProcess(this);
        }

        int padding = 1;
        uniqueName = name;
        while (methodNames.containsValue(uniqueName) && !bodyLess)
            uniqueName = name + '_' + padding++;

        finalName = uniqueName;
        methodNames.put(this, finalName);

        if (parent instanceof Class clazz)
            finalName = clazz.getName() + "." + finalName;
    }

    /**
     * Initialize all class member declarations for the overriding node.
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessMember(Generator generator) {
        // do not recreate the LLVM representation of the method
        // TODO find out why Method#postProcessMember is called twice
        if (defined)
            return;

        this.generator = generator;

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

        IRFunctionType functionType = IRFunctionType.create(returnType, paramTypes, false);

        // create the LLVM function for the target context
        function = IRFunction.create(module, finalName, functionType);

        // handle successful method creation
        defined = true;
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

        if (bodyLess)
            return function;

        // create an entry block for the function
        IRBlock block = IRBlock.create(context, function, "entry");
        builder.positionAtEnd(block);

        // TODO improve automatic void return insertion: only return void if the method type is void,
        //  also check recursively child nodes, if they terminate the method. eg: if both cases of
        //  an if-else pairs return a value, then the method should not insert a return here
        // if (body.isEmpty() || body.get(body.size() - 1).getNodeType() != NodeType.RETURN)
        //     body.add(new Return(null));

        // generate the LLVM instructions for the body of the function
        for (Node node : body)
            node.generate(generator);

        return function;
    }

    public boolean checkTypes(List<Type> types) {
        // System.err.println("check param types " + name + " " + types.size() + " " + parameters.size());

        if (types.size() != parameters.size())
            return false;

        for (int i = 0; i < types.size(); i++) {
            Type checkType = types.get(i);
            Type paramType = parameters.get(i).getType();

            if (checkType instanceof NamedScalarType named
                    && named.getScalarType() instanceof ScalarType scalar
                    && scalar.getName().isPrimitive())
                checkType = scalar;

            // TODO: resolve method param types on preprocess
            if (paramType instanceof ScalarType scalar && !scalar.getName().isPrimitive()) {
                paramType = resolveType(scalar.getName().getDirect());
                if (paramType == null)
                    throw new IllegalStateException("Unable to resolve method parameter type: " + scalar.getName());
            }

            // System.err.println("check " + checkType + " " + checkType.getClass());
            // System.err.println("param " + paramType + " " + paramType.getClass());

            // TODO should resolve type on postProcessUse
            if (paramType instanceof ScalarType scalar && !scalar.getName().isPrimitive())
                paramType = resolveType(scalar.getName().getDirect());

            if (!checkType.equals(paramType))
                return false;
        }

        return true;
    }

    /**
     * Resolve a node from this node context by its name. If the name is unresolved locally,
     * the parent element tries to resolve it.
     * @param name target node name
     * @return resolved node or null if it was not found
     */
    @Override
    public Value resolveName(String name) {
        // test if this is a class method, therefore 'this' is passed as the first argument
        boolean instanceMethod = parent instanceof Class;

        // resolve the 'this' keyword for instance methods
        if (name.equals("this")) {
            // make sure we don't use 'this' in a static context
            if (!instanceMethod)
                throw new IllegalStateException("Trying to access 'this' from a static method.");

            // create a 'this' parameter accessor or get it from the cache
            return paramCache.computeIfAbsent("this", k -> new ImmutableParameterIndexer(0, (Class) parent));
        }

        // resolve method parameters
        for (int i = 0; i < parameters.size(); i++) {
            MethodParameter parameter = parameters.get(i);
            Name paramName = parameter.getName();

            // TODO support compound names
            if (!paramName.isScalar())
                continue;

            // skip the parameter if it does not have the name to be resolved
            String value = ((ScalarName) paramName).getValue();
            if (!value.equals(name))
                continue;

            // increment the field accessing by one, as the first parameter is the instance of 'this', therefore
            // all the remaining parameters are shifter to the right by one
            final int index = i + (instanceMethod ? 1 : 0);

            // get a parameter accessor from the cache or create one if missing
            return paramCache.computeIfAbsent(name, k -> switch (parameter.getType().getReferencing().getType()) {
                case REFERENCE -> new ReferenceParameterIndexer(index, parameter.getType());
                case MUTABLE -> new MutableParameterIndexer(index, parameter.getType());
                case DEREFERENCE, NONE -> new ImmutableParameterIndexer(index, parameter.getType());
            });
        }

        // resolve local variables in the method body
        for (Node node : body) {
            if (node instanceof ImmutableLocalDeclareAssign local && local.getName().equals(name))
                return local;
            else if (node instanceof MutableLocalDeclareAssign local && local.getName().equals(name))
                return local;
            else if (node instanceof ReferenceLocalDeclareAssign local && local.getName().equals(name))
                return local;
        }

        // let the parent nodes recursively resolve the name
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


    @RequiredArgsConstructor
    @NodeInfo(type = NodeType.REFERENCE_PARAMETER_INDEXER)
    private class ReferenceParameterIndexer extends Value implements PointerOwner, Mutable {
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
            return generate(generator);
        }

        @Override
        public IRType getPointerType() {
            return type.generateType(generator.getContext());
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
