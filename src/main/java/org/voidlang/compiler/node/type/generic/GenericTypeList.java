package org.voidlang.compiler.node.type.generic;

import dev.inventex.octa.console.ConsoleFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a holder of a generic type list for a type declaration.
 * <p>Example:</p>
 * <pre> {@code
 *     void foo<T, U = FallbackType>()
 * } </pre>
 */
@AllArgsConstructor
@Getter
public class GenericTypeList implements Iterable<GenericType> {
    /**
     * The generic type tokens of the type. If the diamond operator is used,
     * this list is an empty list.
     */
    @NotNull
    private final List<GenericType> generics;

    /**
     * Indicate, whether the generic types have been declared explicitly.
     */
    private final boolean explicit;

    /**
     * Indicate, whether the generic type declaration uses the diamond operator, therefore
     * is relying on the compiler to find the type from its use.
     * @return true if a diamond operator is explicitly declared
     */
    public boolean isDiamond() {
        return explicit && generics.isEmpty();
    }

    /**
     * Returns an iterator over elements of type {@link GenericArgument}.
     * @return a generic type iterator.
     */
    @NotNull
    @Override
    public Iterator<GenericType> iterator() {
        return generics.iterator();
    }

    /**
     * Get the string representation of the generic type list.
     * @return generic type list debug information
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (!explicit)
            return builder.toString();
        builder
            .append(ConsoleFormat.CYAN)
            .append('<');
        String collect = generics.stream()
            .map(GenericType::toString)
            .collect(Collectors.joining(", "));
        return builder
            .append(collect)
            .append(ConsoleFormat.CYAN)
            .append('>')
            .toString();
    }
}
