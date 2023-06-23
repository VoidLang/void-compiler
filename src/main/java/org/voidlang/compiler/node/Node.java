package org.voidlang.compiler.node;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.voidlang.compiler.node.common.Error;
import org.voidlang.compiler.node.common.Finish;
import org.voidlang.compiler.node.element.Method;
import org.voidlang.llvm.element.IRValue;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import org.voidlang.compiler.node.type.core.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents an instruction node that is parsed from raw tokens.
 * A node can be an exact instruction or a holder of multiple instructions.
 * The node hierarchy is then transformed to executable bytecode.
 */
@Getter
public abstract class Node {
    protected static final Prettier prettier = new Prettier();

    /**
     * The type of the node.
     */
    private final NodeType nodeType;

    private Node parent;

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
     * Initialize all the child nodes for this node.
     * This is
     */
    public void preProcess(Node root) {
        this.parent = root;
        getChildren().forEach(e -> {
            e.parent = this;
            e.preProcess(this);
        });
    }

    /**
     *
     */
    public void postProcess() {
        getChildren().forEach(Node::postProcess);
    }

    private List<Node> getChildren() {
        List<Node> children = new ArrayList<>();
        for (Field field : getClass().getDeclaredFields()) {
            field.setAccessible(true);
            Object value;
            try {
                value = field.get(this);
            } catch (IllegalAccessException e) {
                System.err.println("Unable to initialize parent " + field + " for class " + getClass());
                continue;
            }
            if (value instanceof Node node)
                children.add(node);
            else if (value instanceof List<?> list) {
                ParameterizedType genericType = (ParameterizedType) field.getGenericType();
                Class<?> listType = (Class<?>) genericType.getActualTypeArguments()[0];
                if (!Node.class.isAssignableFrom(listType))
                    continue;
                children.addAll(list
                    .stream()
                    .map(e -> (Node) e)
                    .toList());
            }
            else if (value instanceof Map<?,?> map) {
                ParameterizedType genericType = (ParameterizedType) field.getGenericType();
                Class<?> valueType = (Class<?>) genericType.getActualTypeArguments()[1];
                if (!Node.class.isAssignableFrom(valueType))
                    continue;
                children.addAll(map
                    .values()
                    .stream()
                    .map(e -> (Node) e)
                    .toList());
            }
        }
        return children;
    }

    /**
     * Resolve a type from this node context by its name. If the type is unresolved locally,
     * the parent element tries to resolve it.
     * @param name target type name
     * @return resolved type or null if it was not found
     */
    @Nullable
    public Type resolveType(String name) {
        return parent.resolveType(name);
    }

    /**
     * Resolve a node from this node context by its name. If the name is unresolved locally,
     * the parent element tries to resolve it.
     * @param name target node name
     * @return resolved node or null if it was not found
     */
    @Nullable
    public Node resolveName(String name) {
        return parent.resolveName(name);
    }

    /**
     * Resolve a method from this node context by its name. If the method is unresolved locally,
     * the parent element tries to resolve it.
     * @param name target method name
     * @return resolved method or null if it was not found
     */
    @Nullable
    public Method resolveMethod(String name, List<Type> types) {
        return parent.resolveMethod(name, types);
    }
}
