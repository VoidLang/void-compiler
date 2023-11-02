package parser;

import dev.inventex.octa.console.ConsoleFormat;
import lombok.SneakyThrows;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Pointer;
import org.voidlang.compiler.builder.Application;
import org.voidlang.compiler.builder.Package;
import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.compiler.node.Parser;
import org.voidlang.compiler.node.element.Class;
import org.voidlang.compiler.node.element.Method;
import org.voidlang.compiler.node.element.Struct;
import org.voidlang.compiler.token.Token;
import org.voidlang.compiler.token.TokenType;
import org.voidlang.compiler.token.Tokenizer;
import org.voidlang.compiler.token.Transformer;
import org.voidlang.llvm.element.*;
import util.LLVM;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.llvm.global.LLVM.*;

public class ParserTest {
    public static void main(String[] args) throws Exception {
        List<Token> tokens = tokenizeSource();
        debugTokens(tokens);

        Application application = new Application();
        Generator generator = LLVM.createContext();

        Package root = new Package(application, generator);
        Parser parser = new Parser(root, tokens);

        System.out.println();
        System.out.println(ConsoleFormat.RED + "           " + ConsoleFormat.BOLD + "PARSED NODES");
        System.out.println(ConsoleFormat.DEFAULT);

        Node node;
        List<Node> nodes = new ArrayList<>();
        // preprocess nodes
        do {
            nodes.add(node = parser.next());
            if (node.is(NodeType.ERROR))
                throw new RuntimeException();
            node.preProcess(root);
        } while (node.hasNext());

        // preprocess types
        for (Node e : nodes) {
            if (e instanceof Class clazz) {
                clazz.generateType(generator.getContext());
                root.defineClass(clazz);
            }
            else if (e instanceof Struct struct) {
                struct.generateType(generator.getContext());
                root.defineStruct(struct);
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

        Method main = root.resolveMethod("main", new ArrayList<>());
        debugBitcode(generator, main);
    }

    private static void debugBitcode(Generator generator, Method main) throws Exception {
        IRModule module = generator.getModule();

        System.out.println(ConsoleFormat.RED + "           " + ConsoleFormat.BOLD + "LLVM BITCODE");
        System.out.println(ConsoleFormat.DEFAULT);
        System.out.println(module.print());

        BytePointer error = new BytePointer((Pointer) null);
        if (!module.verify(IRModule.VerifierFailureAction.ABORT_PROCESS, error)) {
            LLVMDisposeMessage(error);
            return;
        }

        ExecutionEngine engine = ExecutionEngine.create();
        MMCJITCompilerOptions options = MMCJITCompilerOptions.create();
        if (!engine.createMCJITCompilerForModule(module, options, error)) {
            System.err.println("Failed to create JIT compiler: " + error.getString());
            LLVMDisposeMessage(error);
        }

        long start = System.currentTimeMillis();
        IRGenericValue result = engine.runFunction(main.getFunction(), new ArrayList<>());
        long end = System.currentTimeMillis();

        System.out.println("Result: " + result.toInt());
        System.out.println("Execution took " + (end - start) + "ms");

        compileAndLinkModule(module);
    }

    private static void compileAndLinkModule(IRModule module) throws Exception {
        File debugDir = new File(System.getProperty("user.dir"), "debug");
        debugDir.mkdir();

        File bitcodeFile = new File(debugDir, "bitcode.bc");
        module.writeBitcodeToFile(bitcodeFile);

        File dumpFile = new File(debugDir, "dump.ll");
        module.printIRToFile(dumpFile);

        File objectFile = new File(debugDir, "test.obj");
        File runFile = new File(debugDir, "run.exe");

        ProcessBuilder compileBuilder = new ProcessBuilder("clang", "-c", "-o",
            objectFile.getAbsolutePath(), bitcodeFile.getAbsolutePath());
        Process compileProcess = compileBuilder.start();
        int compileStatus = compileProcess.waitFor();

        ProcessBuilder linkBuilder = new ProcessBuilder("clang", "-o", runFile.getAbsolutePath(),
            objectFile.getAbsolutePath(), "-luser32", "-lgdi32", "-lkernel32");
        Process linkProcess = linkBuilder.start();
        int linkStatus = linkProcess.waitFor();

        runAndDebugExecutable(runFile);
    }

    private static void runAndDebugExecutable(File exeFile) throws Exception {
        // ProcessBuilder builder = new ProcessBuilder("cmd.exe".split("\\s+")); //exeFile.getAbsolutePath());
        // ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/C", exeFile.getAbsolutePath()); //exeFile.getAbsolutePath());
        ProcessBuilder builder = new ProcessBuilder(exeFile.getAbsolutePath());
        Process process = builder.start();

            // Get the process's stdout stream
            InputStream stdout = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));

            // Read and print each line from stdout
            String line;
            while (true) {
                try {
                    if ((line = reader.readLine()) == null) break;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                System.out.println(line);
            }

        // BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        // writer.write(exeFile.getAbsolutePath());

        int status = process.waitFor();
        System.out.println("Debug exited: " + status);

       // System.err.println(exeFile.getAbsolutePath());
    }

    private static List<Token> tokenizeSource() {
        String data = readSource();

        Tokenizer tokenizer = new Tokenizer(new File("main.vs"), data);
        List<Token> tokens = new ArrayList<>();
        Token token;

        do {
            tokens.add(token = tokenizer.next());
            if (token.is(TokenType.UNEXPECTED))
                throw new RuntimeException(token.getValue());
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
        try (InputStream stream = ParserTest.class.getClassLoader().getResourceAsStream("source.vs");
             BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            String line;
            while ((line = reader.readLine()) != null)
                builder.append(line).append('\n');
        }
        return builder.toString();
    }
}
