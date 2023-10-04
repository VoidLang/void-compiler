package util;

import lombok.experimental.UtilityClass;
import org.voidlang.compiler.builder.Package;
import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.element.Class;
import org.voidlang.compiler.node.element.Method;

import java.util.List;

@UtilityClass
public class Codegen {
    public void generate(Generator generator, Package root, List<Node> nodes) {

        // preprocess types
        for (Node e : nodes) {
            if (e instanceof Class clazz) {
                clazz.generateType(generator.getContext());
                root.defineClass(clazz);
            }
            else if (e instanceof Method method)
                root.defineMethod(method);
        }

        for (Node e : nodes)
            e.postProcessType(generator);

        for (Node e : nodes)
            e.postProcessMember(generator);

        for (Node e : nodes)
            e.postProcessUse(generator);

        // generate bitcode
        for (Node e : nodes)
            e.generate(generator);
    }
}
