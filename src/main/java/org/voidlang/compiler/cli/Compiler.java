package org.voidlang.compiler.cli;

import lombok.RequiredArgsConstructor;
import org.voidlang.compiler.util.Validate;

import java.io.File;

@RequiredArgsConstructor
public class Compiler {
    private final String inputDir;

    public void compile() {
        File projectDir = new File(inputDir);
        if (projectDir.exists() || !projectDir.isDirectory())
            Validate.panic("Project " + inputDir + " does not exist");

        File sourceDir = new File(projectDir, "src");
        if (!sourceDir.exists() || !sourceDir.isDirectory())
            Validate.panic("Project source folder does not exist");

        File buildFile = new File(projectDir, "void.toml");
        if (!buildFile.exists() || !projectDir.isFile())
            Validate.panic("Project void.html file does not exist");
    }
}
