package tokenizer;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.voidlang.compiler.token.Token;
import org.voidlang.compiler.token.TokenType;
import org.voidlang.compiler.token.Tokenizer;
import org.voidlang.compiler.token.Transformer;
import util.Resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TokenizerTest {
    @Test
    public void test() {
        String resource = Resources.read("tokenizer/input.vs");
        List<Token> tokens = tokenizeSource(resource);

        List<Token> expected = Arrays.asList(
            Token.of(TokenType.TYPE,       "void"),
            Token.of(TokenType.IDENTIFIER, "main"),
            Token.of(TokenType.OPEN,       "("),
            Token.of(TokenType.CLOSE,      ")"),
            Token.of(TokenType.BEGIN,      "{"),
            Token.of(TokenType.IDENTIFIER, "println"),
            Token.of(TokenType.OPEN,       "("),
            Token.of(TokenType.STRING,     "Hello, World"),
            Token.of(TokenType.CLOSE,      ")"),
            Token.of(TokenType.SEMICOLON,  "auto"),
            Token.of(TokenType.END,        "}"),
            Token.of(TokenType.SEMICOLON,  "auto"),
            Token.of(TokenType.FINISH)
        );

        assertIterableEquals(expected, tokens);
    }

    private static List<Token> tokenizeSource(String source) {
        Tokenizer tokenizer = new Tokenizer(source);
        List<Token> tokens = new ArrayList<>();
        Token token;

        do {
            tokens.add(token = tokenizer.next());
        } while (token.hasNext());

        return new Transformer(tokens).transform();
    }
}
