package compiler;

import org.voidlang.compiler.cli.Compiler;

import java.io.File;

public class CompileProjectTest {
    public static void main(String[] args) throws Exception {
        File projectDir = new File(System.getProperty("user.dir"), "example");

        new Compiler(projectDir.getAbsolutePath()).compile();
    }
}
