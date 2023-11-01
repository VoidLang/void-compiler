package org.voidlang.compiler.cli;

import com.moandjiezana.toml.Toml;
import dev.inventex.octa.console.ConsoleFormat;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Pointer;
import org.voidlang.compiler.builder.Application;
import org.voidlang.compiler.builder.ImportNode;
import org.voidlang.compiler.builder.Package;
import org.voidlang.compiler.builder.ProjectSettings;
import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.compiler.node.Parser;
import org.voidlang.compiler.node.common.Finish;
import org.voidlang.compiler.node.element.Class;
import org.voidlang.compiler.node.element.Method;
import org.voidlang.compiler.node.element.Struct;
import org.voidlang.compiler.node.info.PackageImport;
import org.voidlang.compiler.node.info.PackageSet;
import org.voidlang.compiler.node.type.modifier.ModifierList;
import org.voidlang.compiler.token.Token;
import org.voidlang.compiler.token.TokenType;
import org.voidlang.compiler.token.Tokenizer;
import org.voidlang.compiler.token.Transformer;
import org.voidlang.compiler.util.Validate;
import org.voidlang.llvm.element.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

import static org.bytedeco.llvm.global.LLVM.*;
import static org.bytedeco.llvm.global.LLVM.LLVMInitializeNativeTarget;

@RequiredArgsConstructor
public class Compiler {
    private final String inputDir;

    private Application application;

    private ProjectSettings settings;

    private File targetDir;

    public void compile() {
        File projectDir = new File(inputDir);
        if (!projectDir.exists() || !projectDir.isDirectory())
            Validate.panic("Project " + inputDir + " does not exist");

        File sourceDir = new File(projectDir, "src");
        if (!sourceDir.exists() || !sourceDir.isDirectory())
            Validate.panic("Project source folder does not exist");

        File buildFile = new File(projectDir, "void.toml");
        if (!buildFile.exists() || !buildFile.isFile())
            Validate.panic("Project void.html file does not exist");

        targetDir = new File(inputDir, "target");
        targetDir.mkdir();

        Toml toml = new Toml().read(buildFile);
        settings = toml
            .getTable("project")
            .to(ProjectSettings.class);

        compileSources(sourceDir);
    }

    @SneakyThrows
    private void compileSources(File sourceDir) {
        application = new Application();

        initLLVM();

        walk(sourceDir);

        postProcessTypes();
        postProcessMembers();
        postProcessUses();
        generate();

        compilePackages();

        linkModules();

        runExecutable();
    }

    @SneakyThrows
    private void runExecutable() {
        File exeFile = new File(targetDir, settings.name + ".exe");

        ProcessBuilder runBuilder = new ProcessBuilder(exeFile.getAbsolutePath());
        Process runProcess = runBuilder.start();

        int status = runProcess.waitFor();

        System.out.println();
        System.out.println("Process exited with code: " + status);;
    }

    private void postProcessTypes() {
        application
            .getPackages()
            .values()
            .forEach(pkg -> pkg.postProcessType(pkg.getGenerator()));
    }

    private void postProcessMembers() {
        application
            .getPackages()
            .values()
            .forEach(pkg -> pkg.postProcessMember(pkg.getGenerator()));
    }

    private void postProcessUses() {
        application
            .getPackages()
            .values()
            .forEach(pkg -> pkg.postProcessUse(pkg.getGenerator()));
    }

    private void generate() {
        application
            .getPackages()
            .values()
            .forEach(pkg -> pkg.generate(pkg.getGenerator()));
    }

    private void compilePackages() {
        application
            .getPackages()
            .values()
            .forEach(pkg -> {
                Generator generator = pkg.getGenerator();

                IRModule module = generator.getModule();

                BytePointer error = new BytePointer((Pointer) null);
                if (!module.verify(IRModule.VerifierFailureAction.ABORT_PROCESS, error)) {
                    LLVMDisposeMessage(error);
                    return;
                }

                compileModule(module);
            });
    }


    private void initLLVM() {
        LLVMInitializeCore(LLVMGetGlobalPassRegistry());
        LLVMLinkInMCJIT();
        LLVMInitializeNativeAsmPrinter();
        LLVMInitializeNativeAsmParser();
        LLVMInitializeNativeTarget();
    }

    @SneakyThrows
    private void linkModules() {
        File exeFile = new File(targetDir, settings.name + ".exe");

        List<String> args = new ArrayList<>(List.of("clang"));

        File objDir = new File(targetDir, "object");

        File[] list = objDir.listFiles();

        if (list == null)
            throw new IllegalStateException("No object files found");

        List<File> files = new ArrayList<>(Arrays.asList(list));

        Collections.reverse(files);

        for (File file : files) {
            if (!file.getName().endsWith(".obj"))
                continue;

            args.add(file.getAbsolutePath());
        }

        args.addAll(List.of("-o", exeFile.getAbsolutePath()));
        args.addAll(List.of("-luser32", "-lgdi32", "-lkernel32"));

        args.add("-v");

        System.out.println(String.join(" ", args));

        ProcessBuilder linkBuilder = new ProcessBuilder(args);
        Process linkProcess = linkBuilder.start();

        System.out.print(ConsoleFormat.DEFAULT);
        System.err.print(ConsoleFormat.DEFAULT);

        linkProcess.getErrorStream().transferTo(System.err);
        linkProcess.getInputStream().transferTo(System.out);

        linkProcess.waitFor();
    }

    private void readSource(File file) {
        List<Token> tokens = tokenizeFile(file);

        String fileName = file.getName();
        String moduleName = fileName.substring(0, fileName.length() - ".vs".length());

        Generator generator = createContext(moduleName);

        if (!tokens.get(0).is(TokenType.INFO, "package"))
            throw new IllegalStateException("Package declaration is missing from file: " + fileName);
        String packageName = tokens.get(1).getValue();

        Package pkg = application.getPackage(packageName);
        if (pkg == null) {
            pkg = new Package(generator);
            application.addPackage(packageName, pkg);
        }

        parsePackage(pkg, tokens);
    }

    private void parsePackage(Package pkg, List<Token> tokens) {
        Generator generator = pkg.getGenerator();
        Parser parser = new Parser(pkg, tokens);

        Node node;
        List<Node> nodes = new ArrayList<>();
        // preprocess nodes
        do {
            nodes.add(node = parser.next());
            if (node.is(NodeType.ERROR))
                throw new RuntimeException();
            node.preProcess(pkg);
        } while (node.hasNext());

        // preprocess types
        for (Node e : nodes) {
            if (e instanceof PackageImport packageImport)
                pkg.addAndMergeImport(packageImport.getNode());
            else if (e instanceof Class clazz) {
                clazz.generateType(generator.getContext());
                pkg.defineClass(clazz);
            }
            else if (e instanceof Struct struct) {
                struct.generateType(generator.getContext());
                pkg.defineStruct(struct);
            }
            else if (e instanceof Method method)
                pkg.defineMethod(method);
        }
    }

    @SneakyThrows
    private void compileModule(IRModule module) {
        File bitcodeDir = new File(targetDir, "bitcode");
        File objDir = new File(targetDir, "object");
        File debugDir = new File(targetDir, "debug");

        bitcodeDir.mkdir();
        objDir.mkdir();
        debugDir.mkdir();

        String fileName = module.getName();

        File bitcodeFile = new File(bitcodeDir, fileName + ".bc");
        module.writeBitcodeToFile(bitcodeFile);

        File dumpFile = new File(debugDir, fileName + ".ll");
        module.printIRToFile(dumpFile);

        File objectFile = new File(objDir, fileName + ".obj");

        ProcessBuilder compileBuilder = new ProcessBuilder("clang", "-c", "-o",
            objectFile.getAbsolutePath(), bitcodeFile.getAbsolutePath());
        Process compileProcess = compileBuilder.start();
        compileProcess.waitFor();
    }

    private List<Token> tokenizeFile(File file) {
        String content = readFile(file);

        Tokenizer tokenizer = new Tokenizer(file, content);
        List<Token> tokens = new ArrayList<>();
        Token token;

        do {
            tokens.add(token = tokenizer.next());
            if (token.is(TokenType.UNEXPECTED))
                throw new RuntimeException(token.getValue());
        } while (token.hasNext());

        return new Transformer(tokens).transform();
    }

    @SneakyThrows
    private String readFile(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
                builder.append(line).append('\n');
            return builder.toString();
        }
    }

    public Generator createContext(String moduleName) {
        IRContext context = IRContext.create();
        IRModule module = IRModule.create(context, moduleName);
        IRBuilder builder = IRBuilder.create(context);

        return new Generator(context, module, builder);
    }

    private void walk(File file) {
        if (file.isFile())
            readSource(file);
        else if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null)
                return;
            for (File child : files)
                walk(child);
        }
    }
}
