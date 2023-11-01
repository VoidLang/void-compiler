package org.voidlang.compiler.builder;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@AllArgsConstructor
@Getter
public class ImportNode {
    private String name;

    private final List<ImportNode> children = new ArrayList<>();

    public void addChild(ImportNode child) {
        children.add(child);
    }

    public void merge(ImportNode other) {
        for (ImportNode otherChild : other.getChildren()) {
            String childName = otherChild.name;
            ImportNode child = getChild(childName);
            if (child == null)
                children.add(otherChild);
            else
                child.merge(otherChild);
        }
    }

    private ImportNode getChild(String name) {
        return children.stream()
            .filter(child -> child.name.equals(name))
            .findFirst()
            .orElse(null);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(name);
        if (!children.isEmpty()) {
            builder.append("::");
            if (children.size() == 1)
                builder.append(children.get(0).toString());
            else {
                builder.append("{");
                Iterator<ImportNode> iterator = children.iterator();
                while (iterator.hasNext()) {
                    builder.append(iterator.next().toString());
                    if (iterator.hasNext())
                        builder.append(",");
                }
                builder.append("}");
            }
        }
        return builder.toString();
    }

    public static void main(String[] args) {
        String[] imports = {
            "std::collections::HashMap",
            "std::collections::HashSet",
            "std::io::{Read, Write}",
            "std::net::{TcpStream, UdpSocket}"
        };
    }

}
