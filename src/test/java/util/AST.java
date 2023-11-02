package util;

import lombok.experimental.UtilityClass;
import org.voidlang.compiler.builder.Application;
import org.voidlang.compiler.builder.Package;
import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.Parser;
import org.voidlang.compiler.token.Token;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class AST {
    public Package parse(List<Token> tokens) {
        // debugTokens(tokens);

        Application application = new Application();
        Generator generator = LLVM.createContext();
        Package root = new Package(application, generator);
        Parser parser = new Parser(root, tokens);

        // System.out.println();
        // System.out.println(ConsoleFormat.RED + "           " + ConsoleFormat.BOLD + "PARSED NODES");
        // System.out.println(ConsoleFormat.DEFAULT);

        Node node;
        List<Node> nodes = new ArrayList<>();
        // preprocess nodes
        do {
            nodes.add(node = parser.next());
            node.preProcess(root);
        } while (node.hasNext());

        Codegen.generate(generator, root, nodes);

        return root;
    }
}
