package org.voidlang.compiler.node.type.core;

import dev.inventex.octa.console.ConsoleFormat;
import lombok.Getter;
import org.voidlang.llvm.element.IRContext;
import org.voidlang.llvm.element.IRType;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a group of nested type entries.
 * The purpose of this class is to be able to hold type groups <strong>recursively</strong>.
 * <pre> {@code
 *     (bool, string) login()
 * } </pre>
 * Here {@code (bool, string)} is a {@link CompoundType} of two {@link ScalarType} elements: {@code bool} and {@code string}.
 */
@Getter
public class CompoundType implements Type {
    /**
     * The list of the held nested type entries.
     */
    private final List<Type> members;

    public CompoundType(List<Type> members) {
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
     * Generate an LLVM type for this type wrapper.
     * @param context LLVM module context
     * @return type ir code wrapper
     */
    @Override
    public IRType generateType(IRContext context) {
        return null;
    }
}
