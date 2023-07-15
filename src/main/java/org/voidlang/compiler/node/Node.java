package org.voidlang.compiler.node;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.voidlang.compiler.node.common.Error;
import org.voidlang.compiler.node.common.Finish;
import org.voidlang.compiler.node.element.Method;
import org.voidlang.compiler.node.value.Value;
import org.voidlang.llvm.element.IRValue;

import org.voidlang.compiler.node.type.core.Type;

import java.util.List;

/**
 * Represents an instruction node that is parsed from raw tokens.
 * A node can be an exact instruction or a holder of multiple instructions.
 * The node hierarchy is then transformed to executable bytecode.
 */
@Getter
public abstract class Node {
    /**
     * The node pretty print debugger.
     */
    protected static final Prettier prettier = new Prettier();

    /**
     * The type of the node.
     */
    private final NodeType nodeType;

    /**
     * The parent node of the overriding node.
     */
    @Setter
    protected Node parent;

    public Node() {
        NodeInfo info = getClass().getAnnotation(NodeInfo.class);
        if (info == null)
            throw new IllegalStateException(getClass().getSimpleName() + " does not have @NodeInfo");
        nodeType = info.type();
    }

    /**
     * Indicate, whether this node has the given type.
     * @param type target type to check
     * @return true if the type matches this node
     */
    public boolean is(NodeType type) {
        return this.nodeType == type;
    }

    /**
     * Indicate, whether this token is not a finish token.
     * @return true if there are more tokens to be parsed
     */
    public boolean hasNext() {
        return !(this instanceof Error)
            && !(this instanceof Finish);
    }

    /**
     * Print the string representation of this node.
     */
    public void debug() {
        prettier.begin(this);
        prettier.content(this);
        prettier.end();
    }

    /**
     * Generate an LLVM instruction for this node
     * @param generator LLVM instruction generation context
     */
    public abstract IRValue generate(Generator generator);

    /**
     * Generate an LLVM instruction for this node.
     * @param generator LLVM instruction generation context
     */
    public IRValue generateNamed(Generator generator, String name) {
        return generate(generator);
    }

    /**
     * Generate an LLVM instruction for this node
     * @param generator LLVM instruction generation context
     */
    public IRValue generateAndLoad(Generator generator) {
        return generate(generator);
    }

    /**
     * Initialize all the child nodes for the overriding node.
     * @param parent parent node of the overriding node
     */
    public abstract void preProcess(Node parent);

    /**
     * Initialize all type declarations for the overriding node.
     * @param generator LLVM code generator
     */
    public abstract void postProcessType(Generator generator);

    /**
     * Initialize all class member declarations for the overriding node.
     * @param generator LLVM code generator
     */
    public abstract void postProcessMember(Generator generator);

    /**
     * Initialize all type uses for the overriding node.
     * @param generator LLVM code generator
     */
    public abstract void postProcessUse(Generator generator);

    /**
     * Resolve a type from this node context by its name. If the type is unresolved locally,
     * the parent element tries to resolve it.
     * @param name target type name
     * @return resolved type or null if it was not found
     */
    @Nullable
    public Type resolveType(String name) {
        return parent != null ? parent.resolveType(name) : null;
    }

    /**
     * Resolve a node from this node context by its name. If the name is unresolved locally,
     * the parent element tries to resolve it.
     * @param name target node name
     * @return resolved node or null if it was not found
     */
    @Nullable
    public Value resolveName(String name) {
        return parent != null ? parent.resolveName(name) : null;
    }

    /**
     * Resolve a method from this node context by its name. If the method is unresolved locally,
     * the parent element tries to resolve it.
     * @param name target method name
     * @return resolved method or null if it was not found
     */
    @Nullable
    public Method resolveMethod(String name, List<Type> types) {
        return parent != null ? parent.resolveMethod(name, types) : null;
    }
}
