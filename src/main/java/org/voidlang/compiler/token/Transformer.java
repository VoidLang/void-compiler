package org.voidlang.compiler.token;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a token transformer that automatically inserts semicolons at the end of lines when it is required.
 */
public class Transformer {
    /**
     * The list of tokens required before the new line for the semicolon to be inserted.
     */
    private final List<Token> requiredBefore = new ArrayList<>();
    {
        requiredBefore.add(Token.of(TokenType.IDENTIFIER));
        requiredBefore.add(Token.of(TokenType.STRING));
        requiredBefore.add(Token.of(TokenType.CHARACTER));
        requiredBefore.add(Token.of(TokenType.BYTE));
        requiredBefore.add(Token.of(TokenType.UBYTE));
        requiredBefore.add(Token.of(TokenType.SHORT));
        requiredBefore.add(Token.of(TokenType.USHORT));
        requiredBefore.add(Token.of(TokenType.DOUBLE));
        requiredBefore.add(Token.of(TokenType.FLOAT));
        requiredBefore.add(Token.of(TokenType.LONG));
        requiredBefore.add(Token.of(TokenType.ULONG));
        requiredBefore.add(Token.of(TokenType.INTEGER));
        requiredBefore.add(Token.of(TokenType.UINTEGER));
        requiredBefore.add(Token.of(TokenType.HEXADECIMAL));
        requiredBefore.add(Token.of(TokenType.BOOLEAN));
        requiredBefore.add(Token.of(TokenType.NULL));
        requiredBefore.add(Token.of(TokenType.EXPRESSION, "break"));
        requiredBefore.add(Token.of(TokenType.EXPRESSION, "continue"));
        requiredBefore.add(Token.of(TokenType.EXPRESSION, "return"));
        requiredBefore.add(Token.of(TokenType.OPERATOR, "++"));
        requiredBefore.add(Token.of(TokenType.OPERATOR, "--"));
        requiredBefore.add(Token.of(TokenType.CLOSE));
        requiredBefore.add(Token.of(TokenType.STOP));
        requiredBefore.add(Token.of(TokenType.END));
    }

    /**
     * The list of tokens forbidden after the new line for the semicolon to be inserted.
     */
    private final List<Token> forbiddenAfter = new ArrayList<>();
    {
        forbiddenAfter.add(Token.of(TokenType.OPERATOR, "="));
        forbiddenAfter.add(Token.of(TokenType.OPERATOR, "+"));
        forbiddenAfter.add(Token.of(TokenType.OPERATOR, "-"));
        forbiddenAfter.add(Token.of(TokenType.OPERATOR, "*"));
        forbiddenAfter.add(Token.of(TokenType.OPERATOR, "/"));
        forbiddenAfter.add(Token.of(TokenType.OPERATOR, "<"));
        forbiddenAfter.add(Token.of(TokenType.OPERATOR, ">"));
        forbiddenAfter.add(Token.of(TokenType.OPERATOR, "?"));
        forbiddenAfter.add(Token.of(TokenType.OPERATOR, "!"));
        forbiddenAfter.add(Token.of(TokenType.OPERATOR, "^"));
        forbiddenAfter.add(Token.of(TokenType.OPERATOR, "&"));
        forbiddenAfter.add(Token.of(TokenType.OPERATOR, "~"));
        forbiddenAfter.add(Token.of(TokenType.OPERATOR, "$"));
        forbiddenAfter.add(Token.of(TokenType.OPERATOR, "."));
        forbiddenAfter.add(Token.of(TokenType.OPERATOR, "%"));
        forbiddenAfter.add(Token.of(TokenType.OPERATOR, "|"));
        forbiddenAfter.add(Token.of(TokenType.EXPRESSION, "where"));
    }

    /**
     * The list of the input tokens to be transformed.
     */
    private final List<Token> tokens;

    /**
     * The currently parsed token.
     */
    private Token token = Token.of(TokenType.NONE);

    /**
     * The previously parsed token.
     */
    private Token lastToken = Token.of(TokenType.NONE);

    /**
     * The next parsed token.
     */
    private Token nextToken = Token.of(TokenType.NONE);

    /**
     * The index of the currently parsed token.
     */
    private int cursor;

    /**
     * Initialize the transformer
     * @param tokens input tokens
     */
    public Transformer(List<Token> tokens) {
        this.tokens = tokens;
    }

    /**
     * Apply the transformer on the tokens.
     * @return token list with inserted semicolons
     */
    public List<Token> transform() {
        List<Token> result = new ArrayList<>();

        // transform tokens while there are more to be parsed
        while (hasNext()) {
            // update the currently parsed tokens
            update();

            // ignore tokens that are a part of a command
            // TODO this should be done by the tokenizer
            handleCommentLine();
            handleCommentBlock();

            // ignore the token if it is not a new line
            if (!token.is(TokenType.NEW_LINE)) {
                if (!token.is(TokenType.NONE))
                    result.add(token);
                continue;
            }

            // check if the token before is one of the required tokens
            boolean requiredBefore = this.requiredBefore
                .stream()
                .anyMatch(element -> equals(element, lastToken));

            // check if the token after is one of the forbidden tokens
            boolean forbiddenAfter = this.forbiddenAfter
                .stream()
                .anyMatch(element -> equals(element, nextToken));

            // place a semicolon if the token before the new line is one of the registered tokens,
            // and the token after the new line is not one of the forbidden tokens
            if (requiredBefore && !forbiddenAfter)
                result.add(Token.of(TokenType.SEMICOLON, "auto"));

            // if the requirements do not meet, we are just going to ignore the token
            // there is no need to put a semicolon, because it seems like the expression
            // continues
            // eg:
            // database.fetchUser() <- new line detected, but a "." follows the token, do not place semicolon
            //     .then(|user| println("hi"))
            // return "hello" <- end of method declaration, place a semicolon after
        }
        return result;
    }

    /**
     * Check if two tokens are equals. Ignore value checking for certain token types.
     * @param left first token to check
     * @param right second token to check
     * @return true if the two tokens are equals
     */
    private boolean equals(Token left, Token right) {
        // make sure both the tokens has the same type
        if (left.getType() != right.getType())
            return false;
        switch (left.getType()) {
            // some tokens' values must be checked as well
            case OPERATOR:
            case EXPRESSION:
                return left.getValue().equals(right.getValue());
            default:
                return true;
        }
    }

    /**
     * Update the currently parsed tokens.
     */
    private void update() {
        token     = get(cursor);
        lastToken = get(cursor - 1);
        nextToken = get(cursor + 1);
        cursor++;
    }

    /**
     * Ignore tokens that belong to a line of comment.
     */
    private void handleCommentLine() {
        // check for line command start
        if (!token.is(TokenType.OPERATOR, "/") || !nextToken.is(TokenType.OPERATOR, "/"))
            return;
        // loop until a new line starts
        while (true) {
            Token token = get(cursor++);
            // we should also account for end of file
            if (token.is(TokenType.NEW_LINE, TokenType.FINISH, TokenType.UNEXPECTED)) {
                update();
                return;
            }
        }
    }

    /**
     * Ignore tokens that belong to a block of comments.
     */
    private void handleCommentBlock() {
        // check for block command start
        if (!token.is(TokenType.OPERATOR, "/") || !nextToken.is(TokenType.OPERATOR, "*"))
            return;
        // loop until the comment block ends
        while (true) {
            Token first  = get(cursor++);
            Token second = get(cursor++);
            // check for the comment block suffix
            if (first.is(TokenType.OPERATOR, "*") && second.is(TokenType.OPERATOR, "/")) {
                update();
                return;
            }
        }
    }

    /**
     * Determine if there are more tokens to be parsed.
     * @return true if there are more tokens
     */
    private boolean hasNext() {
        return cursor >= 0 && cursor < tokens.size();
    }

    /**
     * Safely get the token at the given index.
     * @return token at the given index
     */
    private Token get(int index) {
        return index >= 0 && index < tokens.size() ? tokens.get(index) : Token.of(TokenType.NONE);
    }
}
