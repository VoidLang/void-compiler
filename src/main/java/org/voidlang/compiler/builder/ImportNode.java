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
