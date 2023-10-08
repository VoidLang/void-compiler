package org.voidlang.compiler.builder;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class Application {
    private final Map<String, Package> packages = new HashMap<>();

    public Package getPackage(String name) {
        return packages.get(name);
    }

    public void addPackage(String packageName, Package pkg) {
        packages.put(packageName, pkg);
    }
}
