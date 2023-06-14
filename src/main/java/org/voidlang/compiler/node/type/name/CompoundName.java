package org.voidlang.compiler.node.type.name;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a group of nested name entries.
 * The purpose of this class is to be able to hold name groups <strong>recursively</strong>.
 * <pre> {@code
 *     void foo((int, int) (x, y))
 * } </pre>
 * Here {@code (x, y)} is a {@link CompoundName} of two {@link Name} elements: {@code x} and {@code y}.
 */
@AllArgsConstructor
@Getter
public class CompoundName implements Name {
    /**
     * The list of the held nested name entries.
     */
    private final List<Name> members;

    /**
     * Get the string representation of the type group.
     * @return type group debug information
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("(");
        if (!members.isEmpty()) {
            String collect = members.stream()
                .map(Name::toString)
                .collect(Collectors.joining(", "));
            builder.append(collect);
        }
        return builder.append(')').toString();
    }
}
