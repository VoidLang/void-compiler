package parser;

import org.voidlang.compiler.token.Token;
import org.voidlang.compiler.token.Tokenizer;
import org.voidlang.compiler.token.Transformer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class TokenizerTest {
    public static void main(String[] args) throws Exception {
        String data = readSource();

        Tokenizer tokenizer = new Tokenizer(data);
        List<Token> tokens = new ArrayList<>();
        Token token;

        do {
            tokens.add(token = tokenizer.next());
        } while (token.hasNext());

        tokens = new Transformer(tokens).transform();

        int longest = tokens.stream()
            .mapToInt(x -> x.getType().name().length())
            .max()
            .orElse(0);

        for (Token element : tokens) {
            int length = element.getType().name().length();
            for (int i = 0; i < longest - length; i++)
                System.out.print(' ');
            System.out.println(element);
        }
    }

    private static String readSource() throws Exception {
        StringBuilder builder = new StringBuilder();
        try (InputStream stream = TokenizerTest.class.getClassLoader().getResourceAsStream("source.void");
             BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            String line;
            while ((line = reader.readLine()) != null)
                builder.append(line).append('\n');
        }
        return builder.toString();
    }
}
