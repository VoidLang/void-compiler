package org.voidlang.compiler.token;

import dev.inventex.octa.console.ConsoleFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TokenMeta {
    /**
     * The beginning index of the token.
     */
    private final int beginIndex;

    /**
     * The ending index of the token.
     */
    private final int endIndex;

    /**
     * The index of the first character in the line of the token being processed.
     */
    private final int lineIndex;

    /**
     * The number of the current line being processed for the token.
     */
    private final int lineNumber;

    /**
     * Parse the token range to string.
     * @return token data range
     */
    public String range() {
        return ConsoleFormat.RED + String.valueOf(beginIndex) + ", " + endIndex + ConsoleFormat.WHITE;
    }

    /**
     * Parse the token line information to string.
     * @return token data index
     */
    public String index() {
        return ConsoleFormat.LIGHT_GRAY + String.valueOf(lineNumber) + '(' + lineIndex + ')' + ConsoleFormat.WHITE;
    }
}
