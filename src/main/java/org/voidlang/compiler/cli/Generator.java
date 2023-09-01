package org.voidlang.compiler.cli;

import lombok.RequiredArgsConstructor;
import org.voidlang.compiler.util.Validate;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

@RequiredArgsConstructor
public class Generator {
    private final String projectName;

    public void generate() {
        File workDir = Paths.get(".").toFile();
        File projectDir = new File(workDir, projectName);
        if (projectDir.exists())
            Validate.panic("Project " + projectName + " already exists");

        if (!projectDir.mkdir())
            Validate.panic("Unable to create project folder: " + projectName);

        copyResource("void.toml", projectDir);
        copyResource(".gitignore", projectDir);

        File sourceDir = new File(projectDir, "src");
        if (!sourceDir.mkdir())
            Validate.panic("Unable to create project source folder");

        copyResource("main.vs", sourceDir);
    }

    private void copyResource(String resource, File directory) {
        File file = new File(directory, resource);
        try (InputStream streamIn = getClass().getClassLoader().getResourceAsStream(resource);
             OutputStream streamOut = Files.newOutputStream(file.toPath())) {
            if (streamIn == null) {
                System.out.println("Missing resource " + resource);
                System.exit(0);
            }
            byte[] buffer = new byte[8 * 2024];
            int read;
            while ((read = streamIn.read(buffer)) != -1) {
                streamOut.write(buffer, 0, read);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
