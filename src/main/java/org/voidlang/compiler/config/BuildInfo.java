package org.voidlang.compiler.config;

import lombok.Getter;

/**
 * Represents a package build information holder.
 */
@Getter
public class BuildInfo {
    /**
     * The target platform to compile to.
     */
    public String target;
}
