package org.voidlang.compiler.node.method.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Represents a group of nested type entries.
 * The purpose of this class is to be able to hold type groups <strong>recursively</strong>.
 * <pre> {@code
 *     (bool, string) login()
 * } </pre>
 * Here {@code (bool, string)} is a {@link TypeGroup} of two {@link Type} elements: {@code bool} and {@code string}.
 */
@AllArgsConstructor
@Getter
public class TypeGroup implements TypeEntry {
    /**
     * The list of the held nested type entries.
     */
    private final List<TypeEntry> entries;
}
