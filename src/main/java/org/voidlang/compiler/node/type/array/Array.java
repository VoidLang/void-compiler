package org.voidlang.compiler.node.type.array;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Represents a multidimensional array in the Abstract Syntax Tree.
 * Arrays are fixed-size, so they must be declared explicitly.
 * This means that either a literal value is put inside or a constant
 * identifier is given.
 */
@AllArgsConstructor
@Getter
public class Array {
    /**
     * The list of dimensions in the array.
     */
    private final List<Dimension> dimensions;
}
