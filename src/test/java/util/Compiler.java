package util;

import lombok.experimental.UtilityClass;
import org.voidlang.compiler.builder.Package;
import org.voidlang.compiler.token.Token;

import java.util.List;

@UtilityClass
public class Compiler {
    public Package compile(String resource) {
        String input = Resources.read(resource);
        List<Token> tokens = Tokenizers.tokenizeSource(input);
        return AST.parse(tokens);
    }
}
