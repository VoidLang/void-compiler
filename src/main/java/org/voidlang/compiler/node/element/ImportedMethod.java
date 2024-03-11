package org.voidlang.compiler.node.element;

import lombok.Getter;
import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeInfo;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.compiler.node.local.PassedByReference;
import org.voidlang.compiler.node.type.QualifiedName;
import org.voidlang.compiler.node.type.core.ScalarType;
import org.voidlang.compiler.node.type.core.Type;
import org.voidlang.compiler.node.type.named.NamedScalarType;
import org.voidlang.llvm.element.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter // must override getters, as the values of the parent class are for a different context
@NodeInfo(type = NodeType.IMPORTED_METHOD)
public class ImportedMethod extends Method {
    private final String targetName;

    private Generator generator;

    private Type resolvedType;

    private List<IRType> paramTypes;

    private boolean defined;

    private IRFunction function;

    public ImportedMethod(Method method) {
        super(method.getReturnType(), method.getName(), method.getParameters(), method.getBody());

        targetName = method.getFinalName();
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
            throw new IllegalStateException("Unable to resolve method return type " + getReturnType());

        IRType returnType = resolvedType.generateType(context);

        // make sure to return a pointer for class types
        if (resolvedType instanceof PassedByReference)
            returnType = returnType.toPointerType();

        // generate IR types for the method parameters
        paramTypes = getParameters()
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
        function = IRFunction.create(module, targetName, functionType);

        // handle successful method creation
        defined = true;
    }

    /**
     * Initialize all type declarations for the overriding node.
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessType(Generator generator) {
        // do not post process imported method body
    }

    /**
     * Initialize all type uses for the overriding node.
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessUse(Generator generator) {
        // do not post process imported method body
    }

    /**
     * Generate an LLVM instruction for this node
     * @param generator LLVM instruction generation context
     */
    @Override
    public IRValue generate(Generator generator) {
        // retrieve the pre-declared function declaration
        return function;
    }
}
