package org.voidlang.compiler.node.type.array;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    /**
     * Get the string representation of the scalar type.
     * @return scalar type debug information
     */
    @Override
    public String toString() {
        return dimensions.stream()
            .map(Dimension::toString)
            .collect(Collectors.joining());
    }

    /**
     * Create a new array wrapper that is empty, therefore isn't an array type.
     * @return empty array wrapper
     */
    public static Array noArray() {
        return new Array(new ArrayList<>());
    }

    /**
     * Create an array wrapper of N implicit dimensions.
     * @param dimensions array dimension count
     * @return array of non-sized dimensions
     */
    public static Array implicit(int dimensions) {
        List<Dimension> data = new ArrayList<>();
        for (int i = 0; i < dimensions; i++)
            data.add(Dimension.implicit());
        return new Array(data);
    }

    /**
     * Create an array wrapper of N explicit dimensions, with each dimensions' size specified.
     * @param sizes per-dimension size
     * @return array of constant-sized dimensions
     */
    public static Array explicit(int... sizes) {
        List<Dimension> data = new ArrayList<>();
        for (int size : sizes)
            data.add(Dimension.explicit(size));
        return new Array(data);
    }

    /**
     * Create an array wrapper of N explicit dimensions, with each dimensions having the same size.
     * @param size size for all dimensions
     * @return array of constant-sized dimensions
     */
    public static Array explicit(int size) {
        List<Dimension> data = new ArrayList<>();
        for (int i = 0; i < size; i++)
            data.add(Dimension.explicit(size));
        return new Array(data);
    }

    /**
     * Create an array wrapper of N explicit dimensions, with each dimensions having the same size.
     * @param identifier identifier for constant value of a size for all dimensions
     * @return array of constant-sized dimensions
     */
    public static Array explicit(int dimensions, String identifier) {
        List<Dimension> data = new ArrayList<>();
        for (int i = 0; i < dimensions; i++)
            data.add(Dimension.explicit(identifier));
        return new Array(data);
    }
}