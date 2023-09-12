package org.voidlang.compiler.node.element;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeInfo;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.compiler.node.control.Element;
import org.voidlang.compiler.node.local.PassedByReference;
import org.voidlang.compiler.node.type.core.Type;
import org.voidlang.compiler.node.type.generic.GenericTypeList;
import org.voidlang.llvm.element.IRContext;
import org.voidlang.llvm.element.IRStruct;
import org.voidlang.llvm.element.IRType;
import org.voidlang.llvm.element.IRValue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Getter
@NodeInfo(type = NodeType.CLASS)
public class Class extends Element implements PassedByReference {
    private final String name;

    private final GenericTypeList generics;

    private final List<Node> body;

    private final Map<String, Field> fields = new LinkedHashMap<>();

    private IRStruct struct;

    /**
     * Initialize all the child nodes for the overriding node.
     * @param parent parent node of the overriding node
     */
    @Override
    public void preProcess(Node parent) {
        this.parent = parent;
        int fieldIndex = 0;
        for (Node node : body) {
            node.setParent(this);
            if (node instanceof Field field) {
                field.setFieldIndex(fieldIndex++);
                fields.put(field.getName(), field);
            } else if (node instanceof MultiField multiField) {
                Type type = multiField.getType();
                for (Map.Entry<String, Node> entry : multiField.getValues().entrySet()) {
                    Field field = new Field(type, entry.getKey(), entry.getValue());
                    field.setFieldIndex(fieldIndex++);
                    fields.put(field.getName(), field);
                }
            }
        }
    }

    @Override
    public Field resolveField(String name) {
        return fields.get(name);
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
        IRContext context = generator.getContext();
        List<IRType> members = new ArrayList<>();
        for (Node node : body) {
            if (node instanceof Field field)
                members.add(generateType(context, field.getType()));
            else if (node instanceof MultiField multiField) {
                IRType type = generateType(context, multiField.getType());
                for (int i = 0; i < multiField.getValues().size(); i++)
                    members.add(type);
            }
        }
        struct.setMembers(members);
        return null;
    }

    private IRType generateType(IRContext context, Type type) {
        IRType irType = type.generateType(context);
        if (type instanceof PassedByReference)
            irType = irType.toPointerType();
        return irType;
    }

    /**
     * Generate an LLVM type for this type element type.
     * @param context LLVM module context
     * @return type ir code wrapper
     */
    @Override
    public IRType generateType(IRContext context) {
        // do not generate the struct more than one time
        if (struct != null)
            return struct;
        return struct = IRStruct.define(context, name);
    }

    @Override
    public IRStruct getStructType() {
        return struct;
    }

    @Override
    public IRType getPointerType() {
        return struct.toPointerType();
    }

    @Override
    public String toString() {
        return "Class{"
            + "name='" + name + '\''
           // + ", fields=" + fields
            + '}';
    }
}