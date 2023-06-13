package org.voidlang.compiler.node.type.array;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.voidlang.compiler.token.Token;
import org.voidlang.compiler.token.TokenType;

/**
 * Represents an array dimension holder that specifies the length of an array dimension level.
 */
@AllArgsConstructor
@Getter
public class Dimension {
    /**
     * The size of the array dimension.
     */
    @Nullable
    public final Token size;

    /**
     * Indicate, whether the dimension size was explicitly declared.
     */
    private final boolean explicit;

    /**
     * Indicate, whether the size of this array dimension is specified by a constant integer literal.
     * @return true if the dimension size is given by a hardcoded number
     */
    public boolean isConstant() {
        return size != null && size.is(TokenType.INTEGER);
    }

    /**
     * Get the hardcoded size of this array dimension. Make sure to only use this if {@link #isConstant()}
     * is true. Otherwise, an exception is thrown.
     * @return array dimension size
     */
    public int getSizeConstant() {
        if (!isConstant())
            throw new IllegalStateException("Array dimension is not constant.");
        return Integer.parseInt(size.getValue());
    }
}

