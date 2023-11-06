package org.voidlang.compiler.builder;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeInfo;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.compiler.node.element.Class;
import org.voidlang.compiler.node.element.ImportedMethod;
import org.voidlang.compiler.node.element.Method;
import org.voidlang.compiler.node.element.Struct;
import org.voidlang.compiler.node.type.core.Type;
import org.voidlang.compiler.node.type.named.MethodParameter;
import org.voidlang.llvm.element.IRValue;

import java.util.*;

@RequiredArgsConstructor
@Getter
@NodeInfo(type = NodeType.ROOT)
public class Package extends Node {
    private final Map<String, Package> packages = new HashMap<>();

    private final List<ImportNode> imports = new ArrayList<>();

    private final List<ImportNode> usings = new ArrayList<>();

    private final Map<String, List<Method>> methods = new HashMap<>();

    private final Map<String, Class> classes = new HashMap<>();

    private final Map<String, Struct> structs = new HashMap<>();

    private final Application application;

    private final Generator generator;

    private final String name;

    /**
     * Generate an LLVM instruction for this node
     * @param generator LLVM instruction generation context
     */
    @Override
    public IRValue generate(Generator generator) {
        for (List<Method> methodList : methods.values()) {
            for (Method method : methodList)
                method.generate(generator);
        }

        for (Class clazz : classes.values())
            clazz.generate(generator);

        return null;
    }

    public void resolveImports() {
        // resolve each package import declared in this package
        for (ImportNode node : imports)
            resolveImport(this, node);

        // let each child package resolve its own imports
        packages
            .values()
            .forEach(Package::resolveImports);
    }

    private void resolveImport(Package parent, ImportNode node) {
        String packageName = node.getName();
        // try to resolve the package from nested scope
        Package pkg = parent.getPackages().get(packageName);
        // if the package is not declared locally, try to resolve it from the application root
        if (pkg == null)
            pkg = application.getPackage(packageName);

        // if the package is not declared locally or in the application root, throw an error
        if (pkg == null) {
            List<String> names = new ArrayList<>();
            node.getNameTree(names);
            throw new IllegalStateException("package " + String.join("::", names) + " not found");
        }

        for (ImportNode using : usings) {
            resolveUsing(pkg, using);
        }

        // resolve all package imports of nested import statements
        for (ImportNode child : node.getChildren())
            resolveImport(pkg, child);
    }

    private void resolveUsing(Package target, ImportNode using) {
        String usingName = using.getName();
        if (!usingName.equals(target.getName()))
            return;

        // TODO resolve using wildcards

        for (ImportNode child : using.getChildren()) {
            String childName = child.getName();
            boolean topLevel = child.getChildren().isEmpty();
            boolean wildcard = childName.equals("*");

            // assume the top level is a function, class or any data structure
            if (topLevel) {
                // for now, we will only handle functions
                // TODO handle classes and data structures

                // merge the imported methods, without overlapping the existing ones
                List<Method> targetMethods = !wildcard
                    ? target.getMethods().get(childName)
                    : target // extract the methods from the target package
                        .getMethods()
                        .values()
                        .stream()
                        .reduce(new ArrayList<>(), (a, b) -> {
                            a.addAll(b);
                            return a;
                        });

                if (targetMethods == null) {
                    List<String> names = new ArrayList<>();
                    child.getNameTree(names);
                    throw new IllegalStateException(
                        "method " + String.join("::", names) + "::" + childName + " not found"
                    );
                }

                targetMethods = targetMethods
                    .stream()
                    .map(ImportedMethod::new)
                    .map(method -> (Method) method)
                    .toList();

                List<Method> localMethods = !wildcard
                    ? methods.get(childName)
                    : methods // extract the methods from the local package
                        .values()
                        .stream()
                        .reduce(new ArrayList<>(), (a, b) -> {
                            a.addAll(b);
                            return a;
                        });

                // if there were no methods associated with the name, just add the imported ones
                if (localMethods == null) {
                    methods.put(childName, targetMethods);
                    continue;
                }

                // find all methods from the package, to this package, that's signature does
                // not overlap a method already declared in this package
                List<Method> mergedMethods = new ArrayList<>();
                check: for (Method targetMethod : targetMethods) {
                    for (Method localMethod : localMethods) {
                        // resolve the parameter types of the local method
                        List<Type> paramTypes = localMethod
                            .getParameters()
                            .stream()
                            .map(MethodParameter::getType)
                            .toList();
                        // skip the method, if the signature is already declared locally
                        if (targetMethod.checkTypes(paramTypes))
                            continue check;
                        mergedMethods.add(targetMethod);
                    }
                }

                // merge the non-overlapping methods to this package
                if (!wildcard)
                    methods
                        .computeIfAbsent(childName, name -> new ArrayList<>())
                        .addAll(mergedMethods);
                else
                    for (Method mergedMethod : mergedMethods) {
                        methods
                            .computeIfAbsent(mergedMethod.getName(), name -> new ArrayList<>())
                            .add(mergedMethod);
                    }
            }

            // there are more than one child, so we assume, there are more packages
            else
                resolveUsing(target.getPackages().get(childName), child);
        }
    }

    private void getPackageNames(List<String> names) {
        names.add(0, name);
        if (parent != null)
            names.add(0, ((Package) parent).name);
    }

    /**
     * Initialize all the child nodes for the overriding node.
     * @param parent parent node of the overriding node
     */
    @Override
    public void preProcess(Node parent) {
        this.parent = parent;
        for (List<Method> methodList : methods.values()) {
            for (Method method : methodList)
                method.preProcess(this);
        }
        for (Class clazz : classes.values())
            clazz.preProcess(this);
    }

    /**
     * Initialize all type declarations for the overriding node.
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessType(Generator generator) {
        for (List<Method> methodList : methods.values()) {
            for (Method method : methodList)
                method.postProcessType(generator);
        }
        for (Class clazz : classes.values())
            clazz.postProcessType(generator);
    }

    /**
     * Initialize all class member declarations for the overriding node.
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessMember(Generator generator) {
        for (List<Method> methodList : methods.values()) {
            for (Method method : methodList)
                method.postProcessMember(generator);
        }
        for (Class clazz : classes.values())
            clazz.postProcessMember(generator);
    }

    /**
     * Initialize all type uses for the overriding node.
     * @param generator LLVM code generator
     */
    @Override
    public void postProcessUse(Generator generator) {
        for (List<Method> methodList : methods.values()) {
            for (Method method : methodList)
                method.postProcessUse(generator);
        }
        for (Class clazz : classes.values())
            clazz.postProcessUse(generator);
    }

    @Override
    @Nullable
    public Method resolveMethod(String name, List<Type> types) {
        Method method = resolvePackageMethod(name, types);
        if (method != null)
            return method;

        for (ImportNode node : imports) {
            Package pkg = application.getPackage(node.getName());
            if (pkg == null)
                continue;

        }

        return null;
    }

    public Method resolvePackageMethod(String name, List<Type> types) {
        List<Method> methodList = methods.get(name);
        if (methodList == null)
            return null;
        for (Method method : methodList) {
            if (method.checkTypes(types))
                return method;
        }
        return null;
    }

    @Override
    public @Nullable Type resolveType(String name) {
        Class clazz = classes.get(name);
        if (clazz != null)
            return clazz;
        return structs.get(name);
    }

    public void defineMethod(Method method) {
        methods
            .computeIfAbsent(method.getName(), name -> new ArrayList<>())
            .add(method);
    }

    public void defineClass(Class clazz) {
        classes.put(clazz.getName(), clazz);
    }

    public void defineStruct(Struct struct) {
        structs.put(struct.getName(), struct);
    }

    public void addAndMergeImport(ImportNode target) {
        for (ImportNode node : imports) {
            if (!node.getName().equals(target.getName()))
                continue;

            node.merge(target);
            return;
        }
        imports.add(target);
    }

    public void addAndMergeUsing(ImportNode target) {
        for (ImportNode node : usings) {
            if (!node.getName().equals(target.getName()))
                continue;

            node.merge(target);
            return;
        }
        usings.add(target);
    }
}
