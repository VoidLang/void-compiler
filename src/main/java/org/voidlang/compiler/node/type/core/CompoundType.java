package org.voidlang.compiler.node.type.core;

import dev.inventex.octa.console.ConsoleFormat;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.voidlang.compiler.node.type.named.NamedTypeGroup;
import org.voidlang.compiler.node.type.pointer.Referencing;
import org.voidlang.llvm.element.IRContext;
import org.voidlang.llvm.element.IRStruct;
import org.voidlang.llvm.element.IRType;

import java.util.List;
import java.util.Objects;
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
    @NotNull
    private final Referencing referencing;

    /**
     * The list of the held nested type entries.
     */
    private final List<Type> members;

    private IRStruct struct;

    public CompoundType(@NotNull Referencing referencing, List<Type> members) {
        this.referencing = referencing;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null)
            return false;

        if (o instanceof NamedTypeGroup group)
            return Objects.equals(referencing, group.getReferencing())
                && deepEquals(members, group.getMembers());

        if (!(o instanceof CompoundType that))
            return false;

        return Objects.equals(referencing, that.referencing) && Objects.equals(members, that.members);
    }

    private <T, U> boolean deepEquals(List<T> a, List<U> b) {
        if (a.size() != b.size())
            return false;
        for (int i = 0; i < a.size(); i++) {
            if (!Objects.equals(a.get(i), b.get(i)))
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(referencing, members);
    }

    /**
     * Generate an LLVM type for this type wrapper.
     * @param context LLVM module context
     * @return type ir code wrapper
     */
    @Override
    public IRType generateType(IRContext context) {
        if (struct != null)
            return struct;
        List<IRType> types = members
            .stream()
            .map(member -> member.generateType(context))
            .toList();
        return struct = IRStruct.define(context, "Tuple", types);
    }
}
