package org.voidlang.compiler.config;

import lombok.Getter;

import java.util.List;

/**
 * Represents the package-related configurations of this project
 */
@Getter
public class PackageInfo {
    /**
     * The name of the package that will be used when publishing it.
     */
    public String name;

    /**
     * The current build version of the package
     */
    public String version;

    /**
     * The contributors that are responsible for developing or maintaining the package
     */
    public List<String> authors;
}
