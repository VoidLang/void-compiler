package util;

import lombok.experimental.UtilityClass;
import org.voidlang.compiler.token.Token;
import org.voidlang.compiler.token.TokenType;
import org.voidlang.compiler.token.Tokenizer;
import org.voidlang.compiler.token.Transformer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class Tokenizers {
    public List<Token> tokenizeSource(String source) {
        Tokenizer tokenizer = new Tokenizer(new File(""), source);
        List<Token> tokens = new ArrayList<>();
        Token token;

        do {
            tokens.add(token = tokenizer.next());
            if (token.is(TokenType.UNEXPECTED))
                throw new RuntimeException(token.getValue());
        } while (token.hasNext());

        return new Transformer(tokens).transform();
    }
}
