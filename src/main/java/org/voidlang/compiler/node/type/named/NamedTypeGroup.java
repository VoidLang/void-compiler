package org.voidlang.compiler.node.type.named;

import lombok.Getter;
import org.voidlang.compiler.builder.Package;
import org.voidlang.compiler.node.type.core.ScalarType;
import org.voidlang.compiler.node.type.core.TypeGroup;
import org.voidlang.llvm.element.Builder;
import org.voidlang.llvm.element.Value;

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
@Getter
public class NamedTypeGroup extends NamedType {
    /**
     * The list of the held nested named type entries.
     */
    private final List<NamedType> members;

    public NamedTypeGroup(List<NamedType> members) {
        this.members = members;
    }

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
     * Generate an LLVM instruction for this node
     * @param builder instruction builder for the current context
     * @return node ir code wrapper
     */
    @Override
    public Value generate(Builder builder) {
        return null;
    }
}
