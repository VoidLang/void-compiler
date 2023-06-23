package parser;

import dev.inventex.octa.console.ConsoleFormat;
import lombok.SneakyThrows;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Pointer;
import org.voidlang.compiler.builder.Package;
import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.Parser;
import org.voidlang.compiler.node.element.Class;
import org.voidlang.compiler.node.element.Method;
import org.voidlang.compiler.token.Token;
import org.voidlang.compiler.token.Tokenizer;
import org.voidlang.compiler.token.Transformer;
import org.voidlang.llvm.element.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.llvm.global.LLVM.*;

public class ParserTest {
    public static void main(String[] args) {
        List<Token> tokens = tokenizeSource();
        debugTokens(tokens);

        Package root = new Package();
        Parser parser = new Parser(root, tokens);

        System.out.println();
        System.out.println(ConsoleFormat.RED + "           " + ConsoleFormat.BOLD + "PARSED NODES");
        System.out.println(ConsoleFormat.DEFAULT);

        Generator generator = initLLVM();

        Node node;
        List<Node> nodes = new ArrayList<>();
        // preprocess nodes
        do {
            nodes.add(node = parser.next());
            node.preProcess(root);
        } while (node.hasNext());

        // preprocess types
        for (Node e : nodes) {
            if (e instanceof Class clazz)
                clazz.generateType(generator.getContext());
            else if (e instanceof Method method)
                root.defineMethod(method);
        }

        for (Node e : nodes)
            e.postProcess();

        // generate bitcode
        for (Node e : nodes)
            e.generate(generator);

        Method main = root.resolveMethod("main", new ArrayList<>());
        debugBitcode(generator, main);
    }

    private static Generator initLLVM() {
        LLVMInitializeCore(LLVMGetGlobalPassRegistry());
        LLVMLinkInMCJIT();
        LLVMInitializeNativeAsmPrinter();
        LLVMInitializeNativeAsmParser();
        LLVMInitializeNativeTarget();

        IRContext context = IRContext.create();
        IRModule module = IRModule.create(context, "test_module");
        IRBuilder builder = IRBuilder.create(context);

        return new Generator(context, module, builder);
    }

    private static void debugBitcode(Generator generator, Method main) {
        IRModule module = generator.getModule();

        BytePointer error = new BytePointer((Pointer) null);
        if (!module.verify(IRModule.VerifierFailureAction.PRINT_MESSAGE, error)) {
            System.err.println("Error: " + error.getString());
            LLVMDisposeMessage(error);
            return;
        }

        System.out.println(ConsoleFormat.RED + "           " + ConsoleFormat.BOLD + "LLVM BITCODE");
        System.out.println(ConsoleFormat.DEFAULT);
        System.out.println(module.print());

        ExecutionEngine engine = ExecutionEngine.create();
        MMCJITCompilerOptions options = MMCJITCompilerOptions.create();
        if (!engine.createMCJITCompilerForModule(module, options, error)) {
            System.err.println("Failed to create JIT compiler: " + error.getString());
            LLVMDisposeMessage(error);
        }

        IRGenericValue result = engine.runFunction(main.getFunction(), new ArrayList<>());
        System.out.println("Result: " + result.toInt());
    }

    private static List<Token> tokenizeSource() {
        String data = readSource();

        Tokenizer tokenizer = new Tokenizer(data);
        List<Token> tokens = new ArrayList<>();
        Token token;

        do {
            tokens.add(token = tokenizer.next());
        } while (token.hasNext());

        return new Transformer(tokens).transform();
    }

    private static void debugTokens(List<Token> tokens) {
        System.out.println();
        System.out.println(ConsoleFormat.RED + "           " + ConsoleFormat.BOLD + "PARSED TOKENS");
        System.out.println(ConsoleFormat.DEFAULT);

        int longestType = tokens.stream()
            .filter(x -> x.getMeta() != null)
            .mapToInt(x -> x.getType().name().length())
            .max()
            .orElse(0);

        int longestValue = tokens.stream()
            .filter(x -> x.getMeta() != null)
            .mapToInt(x -> x.getValue().length())
            .max()
            .orElse(0);

        int longestRange = tokens.stream()
            .filter(x -> x.getMeta() != null)
            .mapToInt(x -> x.getMeta().range().length())
            .max()
            .orElse(0);

        for (Token element : tokens) {
            int typeLength = element.getType().name().length();
            for (int i = 0; i < longestType - typeLength; i++)
                System.out.print(' ');
            System.out.print(element);

            if (element.getMeta() == null || element.getMeta().getBeginIndex() < 0) {
                System.out.println();
                continue;
            }

            int valueLength = element.getValue().length();
            for (int i = 0; i < (longestValue - valueLength) + 1; i++)
                System.out.print(' ');

            int rangeLength = element.getMeta().range().length();
            for (int i = 0; i < longestRange - rangeLength; i++)
                System.out.print(' ');

            System.out.print(element.getMeta().range());

            int indexLength = element.getMeta().index().length();
            for (int i = 0; i < 5; i++)
                System.out.print(' ');

            System.out.println(element.getMeta().index());
        }
    }

    @SneakyThrows
    private static String readSource() {
        StringBuilder builder = new StringBuilder();
        try (InputStream stream = ParserTest.class.getClassLoader().getResourceAsStream("source.void");
             BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            String line;
            while ((line = reader.readLine()) != null)
                builder.append(line).append('\n');
        }
        return builder.toString();
    }
}
