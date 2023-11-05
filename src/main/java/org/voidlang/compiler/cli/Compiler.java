package org.voidlang.compiler.cli;

import com.moandjiezana.toml.Toml;
import dev.inventex.octa.console.ConsoleFormat;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Pointer;
import org.jetbrains.annotations.NotNull;
import org.voidlang.compiler.builder.Application;
import org.voidlang.compiler.builder.Package;
import org.voidlang.compiler.builder.ProjectSettings;
import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.compiler.node.Parser;
import org.voidlang.compiler.node.element.Class;
import org.voidlang.compiler.node.element.Method;
import org.voidlang.compiler.node.element.Struct;
import org.voidlang.compiler.node.info.PackageImport;
import org.voidlang.compiler.node.info.PackageUsing;
import org.voidlang.compiler.token.Token;
import org.voidlang.compiler.token.TokenType;
import org.voidlang.compiler.token.Tokenizer;
import org.voidlang.compiler.token.Transformer;
import org.voidlang.compiler.util.Prettier;
import org.voidlang.compiler.util.Validate;
import org.voidlang.llvm.element.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.*;

import static org.bytedeco.llvm.global.LLVM.*;

@RequiredArgsConstructor
public class Compiler {
    private final List<File>
        bitcodeFiles = new ArrayList<>(),
        dumpFiles = new ArrayList<>(),
        objectFiles = new ArrayList<>();

    private final String inputDir;

    private Application application;

    private ProjectSettings settings;

    private File targetDir, sourceDir;


    public void compile() {
        File projectDir = new File(inputDir);
        if (!projectDir.exists() || !projectDir.isDirectory())
            Validate.panic("Project " + inputDir + " does not exist");

        sourceDir = new File(projectDir, "src");
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

        Prettier.setEnabled(false);

        compileSources();
    }

    @SneakyThrows
    private void compileSources() {
        application = new Application();

        initLLVM();

        walkDir(sourceDir).forEach(this::readSource);
        System.out.println();

        resolveImports();
        postProcessTypes();
        postProcessMembers();
        postProcessUses();
        generate();

        compilePackages();
        System.out.println();

        // remove files that are not made be the current compilation
        // TODO improve this logic, do not remove files that are not recompiled, because of being cached
        // removeOldFiles();

        linkModules();

        runExecutable();
    }

    private void removeOldFiles() {
        walkDir(targetDir).forEach(file -> {
            String name = file.getName();
            if (
                (name.endsWith(".bc") && !bitcodeFiles.contains(file)) ||
                (name.endsWith(".ll") && !dumpFiles.contains(file)) ||
                (name.endsWith(".obj") && !objectFiles.contains(file))
            )
                file.delete();
        });
    }

    @SneakyThrows
    private void runExecutable() {
        File exeFile = new File(targetDir, settings.name + ".exe");

        ProcessBuilder runBuilder = new ProcessBuilder(exeFile.getAbsolutePath());
        Process runProcess = runBuilder.start();

        int status = runProcess.waitFor();

        System.out.println();
        System.out.println(
            ConsoleFormat.RED + "" + ConsoleFormat.BOLD +
            "[Debug]: process exited with code: " +
            ConsoleFormat.WHITE + status +
            ConsoleFormat.DEFAULT
        );
    }


    private void resolveImports() {
        application
            .getPackages()
            .values()
            .forEach(Package::resolveImports);
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
        File objDir = new File(targetDir, "object");

        File[] list = objDir.listFiles();
        if (list == null)
            throw new IllegalStateException("No object files found");

        List<File> files = new ArrayList<>(Arrays.asList(list));
        Collections.reverse(files);

        List<String> args = new ArrayList<>(List.of("clang"));
        for (File file : files) {
            if (!file.getName().endsWith(".obj"))
                continue;

            args.add(file.getAbsolutePath());
        }

        args.addAll(List.of("-o", exeFile.getAbsolutePath()));
        args.addAll(List.of("-luser32", "-lgdi32", "-lkernel32"));

        // args.add("-v");

        ProcessBuilder linkBuilder = new ProcessBuilder(args);
        Process linkProcess = linkBuilder.start();

        System.out.print(ConsoleFormat.DEFAULT);
        System.err.print(ConsoleFormat.DEFAULT);

        linkProcess
            .getErrorStream()
            .transferTo(System.err);
        linkProcess
            .getInputStream()
            .transferTo(System.out);

        linkProcess.waitFor();

        System.out.println(
            ConsoleFormat.DARK_GRAY + "" + ConsoleFormat.BOLD + "[" + ConsoleFormat.MAGENTA + "Void" +
            ConsoleFormat.DARK_GRAY + "] " +
            ConsoleFormat.BOLD + ConsoleFormat.GREEN + "compiled and linked successfully" +
            ConsoleFormat.DEFAULT
        );
    }

    private void readSource(File file) {
        String moduleName = file
            .getAbsolutePath()
            .substring(sourceDir.getAbsolutePath().length() + 1)
            .replace('\\', '/');

        String checksum = getChecksum(file);
        String cachedChecksum = getCachedChecksum(file);

        if (checksum != null && checksum.equals(cachedChecksum)) {
            System.out.println(
                ConsoleFormat.DARK_GRAY + "" + ConsoleFormat.BOLD + "[" + ConsoleFormat.MAGENTA + "Void" +
                ConsoleFormat.DARK_GRAY + "] " +
                ConsoleFormat.CYAN + "cached" + ConsoleFormat.LIGHT_GRAY + " > " +
                ConsoleFormat.WHITE + moduleName
            );
            return;
        }

        List<Token> tokens = tokenizeFile(file);

        System.out.println(
            ConsoleFormat.DARK_GRAY + "" + ConsoleFormat.BOLD + "[" + ConsoleFormat.MAGENTA + "Void" +
            ConsoleFormat.DARK_GRAY + "] " +
            ConsoleFormat.YELLOW +  "source" + ConsoleFormat.LIGHT_GRAY + " > " +
            ConsoleFormat.WHITE + moduleName
        );

        Generator generator = createContext(moduleName);

        if (!tokens.get(0).is(TokenType.INFO, "package"))
            throw new IllegalStateException("Package declaration is missing from file: " + file);

        String packageName = tokens
            .get(1)
            .getValue();

        Package pkg = application.getPackage(packageName);
        if (pkg == null) {
            pkg = new Package(application, generator, packageName);
            application.addPackage(packageName, pkg);
        }

        parsePackage(pkg, tokens);

        setFileChecksum(file, checksum);
    }

    private String getChecksum(File file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream stream = Files.newInputStream(file.toPath())) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = stream.read(buffer)) > 0)
                    digest.update(buffer, 0, read);
            }
            byte[] hash = digest.digest();
            StringBuilder builder = new StringBuilder();
            for (byte b : hash)
                builder.append(String.format("%02x", b));
            return builder.toString();
        } catch (Exception ignored) {
            return null;
        }
    }

    private String getCachedChecksum(File file) {
        File dataDir = new File(targetDir, "data");
        if (!dataDir.isDirectory())
            return null;

        String fileName = file
            .getAbsolutePath()
            .substring(sourceDir.getAbsolutePath().length() + 1)
            .replace('\\', '/')
            .replace('/', '.');

        File checksumFile = new File(dataDir, fileName + ".checksum");
        if (!checksumFile.exists())
            return null;

        String content = readFile(checksumFile);

        // remove additional newline
        return content.substring(0, content.length() - 1);
    }

    private void setFileChecksum(File file, String checksum) {
        File dataDir = new File(targetDir, "data");
        dataDir.mkdir();

        String fileName = file
            .getAbsolutePath()
            .substring(sourceDir.getAbsolutePath().length() + 1)
            .replace('\\', '/')
            .replace('/', '.');

        File checksumFile = new File(dataDir, fileName + ".checksum");
        try {
            Files.writeString(checksumFile.toPath(), checksum);
        } catch (Exception ignored) {
        }
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
            else if (e instanceof PackageUsing packageUsing)
                pkg.addAndMergeUsing(packageUsing.getNode());
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
        fileName = fileName
            .substring(0, fileName.length() - 3)
            .replace('/', '.')
            .replace('\\', '.');

        // convert the module to LLVM bitcode representation
        File bitcodeFile = new File(bitcodeDir, fileName + ".bc");
        bitcodeFiles.add(bitcodeFile);
        module.writeBitcodeToFile(bitcodeFile);

        File dumpFile = new File(debugDir, fileName + ".ll");
        dumpFiles.add(dumpFile);
        module.printIRToFile(dumpFile);

        File objectFile = new File(objDir, fileName + ".obj");
        objectFiles.add(objectFile);

        // use clang to convert the LLVM bitcode file to an object file
        ProcessBuilder compileBuilder = new ProcessBuilder("clang", "-c", "-o",
            objectFile.getAbsolutePath(), bitcodeFile.getAbsolutePath());
        Process compileProcess = compileBuilder.start();
        compileProcess.waitFor();

        System.out.println(
            ConsoleFormat.DARK_GRAY + "" + ConsoleFormat.BOLD + "[" + ConsoleFormat.MAGENTA + "Void" +
            ConsoleFormat.DARK_GRAY + "] " +
            ConsoleFormat.RED + ConsoleFormat.BOLD + "compile" +
            ConsoleFormat.LIGHT_GRAY + " > " +
            ConsoleFormat.WHITE + fileName
        );
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
    private @NotNull String readFile(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
                builder
                    .append(line)
                    .append('\n');
            return builder.toString();
        }
    }

    public Generator createContext(String moduleName) {
        IRContext context = IRContext.create();
        IRModule module = IRModule.create(context, moduleName);
        IRBuilder builder = IRBuilder.create(context);

        return new Generator(context, module, builder);
    }

    private List<File> walkDir(File dir) {
        List<File> files = new ArrayList<>();
        if (dir.isFile())
            files.add(dir);
        else if (dir.isDirectory()) {
            File[] list = dir.listFiles();
            if (list == null)
                return files;
            for (File child : list)
                files.addAll(walkDir(child));
        }
        return files;
    }
}
