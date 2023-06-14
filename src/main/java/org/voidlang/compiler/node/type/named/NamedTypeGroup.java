package org.voidlang.compiler.node.type.named;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.voidlang.compiler.node.type.core.ScalarType;
import org.voidlang.compiler.node.type.core.Type;
import org.voidlang.compiler.node.type.core.TypeGroup;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a group of nested named type entries.
 * The purpose of this class is to be able to hold named type groups <strong>recursively</strong>.
 * <pre> {@code
 *     (bool success, (string token, string msg)) login()
 * } </pre>
 * Here {@code (bool, string)} is a {@link TypeGroup} of two {@link ScalarType} elements: {@code bool} and {@code string}.
 */
@AllArgsConstructor
@Getter
public class NamedTypeGroup implements NamedType {
    /**
     * The list of the held nested named type entries.
     */
    private final List<NamedType> members;

    /**
     * Get the string representation of the named type group.
     * @return named type group debug information
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("(");
        if (!members.isEmpty()) {
            String collect = members.stream()
                .map(NamedType::toString)
                .collect(Collectors.joining(", "));
            builder.append(collect);
        }
        return builder.append(')').toString();
    }
}
