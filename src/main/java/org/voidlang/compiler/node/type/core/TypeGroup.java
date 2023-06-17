package org.voidlang.compiler.node.type.core;

import dev.inventex.octa.console.ConsoleFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.voidlang.llvm.element.Builder;
import org.voidlang.llvm.element.Value;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a group of nested type entries.
 * The purpose of this class is to be able to hold type groups <strong>recursively</strong>.
 * <pre> {@code
 *     (bool, string) login()
 * } </pre>
 * Here {@code (bool, string)} is a {@link TypeGroup} of two {@link ScalarType} elements: {@code bool} and {@code string}.
 */
@Getter
public class TypeGroup extends Type {
    /**
     * The list of the held nested type entries.
     */
    private final List<Type> members;

    public TypeGroup(List<Type> members) {
        this.members = members;
    }

    /**
     * Get the string representation of the type group.
     * @return type group debug information
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder()
            .append(ConsoleFormat.CYAN)
            .append("(");
        if (!members.isEmpty()) {
            String collect = members.stream()
                .map(Type::toString)
                .collect(Collectors.joining(ConsoleFormat.CYAN + ", "));
            builder.append(collect);
        }
        return builder
            .append(ConsoleFormat.CYAN)
            .append(')')
            .toString();
    }

    /**
     * Generate an LLVM instruction for this node
     * @param builder instruction builder for the current context
     * @return node ir code wrapper
     */
    @Override
    public Value generate(Builder builder) {
        return null;
    }
}
