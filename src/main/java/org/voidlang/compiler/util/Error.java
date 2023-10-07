package org.voidlang.compiler.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Error {
    INVALID_TOKEN(101),
    INVALID_ESCAPE_SEQUENCE(102),
    MISSING_STRING_TERMINATOR(103),
    INVALID_UNSIGNED_LITERAL(104),
    MULTIPLE_DECIMAL_POINTS(105),
    CANNOT_HAVE_DECIMAL_POINT(106),;

    private final int code;
}
