package org.voidlang.compiler.node.type.named;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.voidlang.compiler.node.type.core.ScalarType;
import org.voidlang.compiler.node.type.core.CompoundType;
import org.voidlang.compiler.node.type.pointer.Referencing;
import org.voidlang.llvm.element.IRContext;
import org.voidlang.llvm.element.IRStruct;
import org.voidlang.llvm.element.IRType;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a group of nested named type entries.
 * The purpose of this class is to be able to hold named type groups <strong>recursively</strong>.
 * <pre> {@code
 *     (bool success, (string token, string msg)) login()
 * } </pre>
 * Here {@code (bool, string)} is a {@link CompoundType} of two {@link ScalarType} elements: {@code bool} and {@code string}.
 */
@RequiredArgsConstructor
@Getter
public class NamedTypeGroup extends NamedType {
    private final Referencing referencing;

    /**
     * The list of the held nested named type entries.
     */
    private final List<NamedType> members;

    private IRStruct struct;

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

    /**
     * Generate an LLVM type for this type wrapper
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
