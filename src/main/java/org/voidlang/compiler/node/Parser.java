package org.voidlang.compiler.node;

import dev.inventex.octa.data.primitive.Tuple;
import org.jetbrains.annotations.NotNull;
import org.voidlang.compiler.node.common.Error;
import org.voidlang.compiler.node.common.Finish;
import org.voidlang.compiler.node.info.PackageImport;
import org.voidlang.compiler.node.info.PackageSet;
import org.voidlang.compiler.node.type.generic.GenericArgumentList;
import org.voidlang.compiler.node.type.QualifiedName;
import org.voidlang.compiler.node.type.core.ScalarType;
import org.voidlang.compiler.node.type.core.Type;
import org.voidlang.compiler.node.type.core.TypeGroup;
import org.voidlang.compiler.node.type.generic.GenericArgument;
import org.voidlang.compiler.node.type.generic.GenericType;
import org.voidlang.compiler.node.type.generic.GenericTypeList;
import org.voidlang.compiler.node.type.modifier.ModifierBlock;
import org.voidlang.compiler.node.type.modifier.ModifierList;
import org.voidlang.compiler.node.type.named.NamedScalarType;
import org.voidlang.compiler.node.type.named.NamedType;
import org.voidlang.compiler.node.type.named.NamedTypeGroup;
import org.voidlang.compiler.token.Token;
import org.voidlang.compiler.token.TokenType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a parser that transforms raw tokens to instruction nodes.
 */
public class Parser {
    /**
     * The map of the operation's precedences and associativity.
     */
    private final Map<String, Tuple<Integer, Integer>> operationPriority = new HashMap<>();
    {
        operationPriority.put("+", new Tuple<>(1, 0));
        operationPriority.put("-", new Tuple<>(1, 0));
        operationPriority.put("*", new Tuple<>(2, 0));
        operationPriority.put("/", new Tuple<>(2, 0));
        operationPriority.put("%", new Tuple<>(2, 0));
        operationPriority.put("^", new Tuple<>(3, 1));
        operationPriority.put(".", new Tuple<>(4, 0));
    }

    /**
     * The target package of the node parser.
     */
    private final Package pkg;

    /**
     * The list of the tokens to be parsed.
     */
    private final List<Token> tokens;

    /**
     * The index of the currently parsed token.
     */
    private int cursor;

    /**
     * Initialize the token parser.
     * @param pkg node parser package
     * @param tokens list of tokens to be parsed
     */
    public Parser(Package pkg, List<Token> tokens) {
        this.pkg = pkg;
        this.tokens = tokens;
    }

    /**
     * Parse the next instruction node.
     * @return new instruction node
     */
    public Node next() {
        // handle end of file
        if (peek().is(TokenType.FINISH))
            return new Finish();

        // handle package declaration
        else if (peek().is(TokenType.INFO, "package"))
            return nextPackage();

        // handle package import
        else if (peek().is(TokenType.INFO, "import"))
            return nextImport();

        // handle modifier list or block declaration
        else if (peek().is(TokenType.MODIFIER))
            return nextModifiers();

        // handle method or type declaration
        else if (peek().is(TokenType.TYPE, TokenType.IDENTIFIER, TokenType.OPEN, TokenType.EXPRESSION))
            return nextTypeOrMethod();
        // handle unexpected token
        System.out.println("Error (Next) " + peek());
        return new Error();
    }

    /**
     * Parse the next package declaration.
     * @return new declared package
     */
    public Node nextPackage() {
        // handle package declaration
        get(TokenType.INFO, "package");
        // get the name of the package
        String name = get(TokenType.STRING).getValue();
        // ensure that the package is ended by a semicolon
        get(TokenType.SEMICOLON);
        System.out.println("package \"" + name + '"');
        return new PackageSet(pkg, name);
    }

    /**
     * Parse the next package import.
     * @return new package import
     */
    public Node nextImport() {
        // handle package import
        get(TokenType.INFO, "import");
        // get the name of the package
        String name = get(TokenType.STRING).getValue();
        // ensure that the package is ended by a semicolon
        get(TokenType.SEMICOLON);
        System.out.println("import \"" + name + '"');
        return new PackageImport(pkg, name);
    }

    /**
     * Parse the next modifier list or block.
     * @return new modifier list or block
     */
    public Node nextModifiers() {
        List<String> modifiers = new ArrayList<>();
        while (peek().is(TokenType.MODIFIER))
            modifiers.add(get().getValue());
        // handle modifier block
        if (peek().is(TokenType.COLON)) {
            // skip the ':' symbol
            get();
            System.out.println(String.join(" ", modifiers) + ": ");
            return new ModifierBlock(pkg, modifiers);
        }
        // handle normal modifier list
        System.out.println(String.join(" ", modifiers) + " ");
        return new ModifierList(pkg, modifiers);
    }

    /**
     * Parse the next type or method declaration.
     * @return new declared type or method
     */
    public Node nextTypeOrMethod() {
        // handle modifier list or block declaration
        if (peek().is(TokenType.MODIFIER))
            return nextModifiers();
            // handle package method declaration
        else if (peek().is(TokenType.TYPE) || peek().is(TokenType.IDENTIFIER))
            return nextMethod();
            // handle multi-return method
        else if (peek().is(TokenType.OPEN))
            return nextMethod();
            // handle package type declaration
        else if (peek().is(TokenType.EXPRESSION))
            return nextTypeDeclaration();
        // handle unexpected token
        Token error = peek();
        System.out.println("Error (Type/Method) " + error);
        return new Error();
    }

    /**
     * Parse the next type declaration.
     * @return new declared type
     */
    public Node nextTypeDeclaration() {
        // get the kind of the type
        // class MyClass {
        // ^^^^^ the expression indicates the kind of the type
        String kind = get(TokenType.EXPRESSION).getValue();

        // get the name of the type
        // class Test {
        //       ^^^^ the identifier indicates the name of the type
        String name = get(TokenType.IDENTIFIER).getValue();

        // handle type generic declaration
        // struct MyGenericStruct<T, U> {
        //                       ^^^^^^ the generic names are placed in between angle brackets
        // class Collection<T = Document> {
        //                    ^^^^^^^^^^^ generic types may have a default value
        GenericTypeList genericTypes = nextGenericTypes();

        System.out.println(kind + " " + name);
        if (genericTypes.isExplicit()) {
            String debug = genericTypes.getGenerics().stream()
                .map(GenericType::toString)
                .collect(Collectors.joining(", "));
            System.out.print("<" + String.join(", ", debug) + ">");
        }

        // TODO generic type implementation (where T implements MyType)



        return null;
    }

    private NamedType nextNamedType() {
        return null;
    }

    /**
     * Parse the next group of named types. Handle nested members recursively.
     * <p>Example:</p>
     * <pre> {@code
     *     (bool success, string msg)
     * } </pre>
     * Here {@code bool success} and {@code string msg} are parsed as two separate type entries.
     * @return next named type group
     */
    private NamedTypeGroup nextNamedGroup() {
        List<NamedType> entries = new ArrayList<>();
        // skip the '(' symbol
        get(TokenType.OPEN);

        while (!peek().is(TokenType.CLOSE)) {
            // parse the next member of the group
            NamedType member = nextNamedType();
            // continue parsing if there are more members expected
            if (peek(TokenType.COMMA, TokenType.CLOSE).is(TokenType.COMMA))
                get();
            // stop parsing if the group has been closed
            else
                break;
        }

        // skip the ')' symbol
        get(TokenType.CLOSE);

        return new NamedTypeGroup(entries);
    }

    private NamedScalarType nextExactNamedType() {
        return null;
    }

    private Type nextType() {
        return null;
    }

    private TypeGroup nextTypeGroup() {
        List<Type> members = new ArrayList<>();
        // skip the '(' symbol
        get(TokenType.OPEN);

        while (!peek().is(TokenType.CLOSE)) {
            // parse the next member of the group
            Type member = nextType();
            // continue parsing if there are more members expected
            if (peek(TokenType.COMMA, TokenType.CLOSE).is(TokenType.COMMA))
                get();
                // stop parsing if the group has been closed
            else
                break;
        }

        // skip the ')' symbol
        get(TokenType.CLOSE);

        return new TypeGroup(members);
    }

    private ScalarType nextScalarType() {
        return null;
    }

    /**
     * Parse the next generic type declaration.
     * <pre> {@code
     *     void foo<T, U = FallbackType>()
     * } </pre>
     * @return generic type tokens
     */
    private GenericTypeList nextGenericTypes() {
        List<GenericType> types = new ArrayList<>();

        // handle no generic type declaration
        if (!peek().is(TokenType.OPERATOR, "<"))
            return new GenericTypeList(types, false);

        // handle the '<' symbol that starts the generic type
        get();

        // assuming that the code might look something like Map<UUID, List<Data>>
        // it is 1 by default, because the first '<' has been already handled
        int offset = 1;

        List<Token> generics = new ArrayList<>();

        // loop until the generic type declaration ends
        while (true) {
            Token token = get();
            // handle nested generic type
            if (token.is(TokenType.OPERATOR, "<"))
                offset++;
                // handle generic type end
            else if (token.is(TokenType.OPERATOR, ">") && --offset == 0)
                break;
            // check if the generic type wasn't terminated closed properly before closing
            else if (token.is(TokenType.OPEN, TokenType.CLOSE))
                throw new IllegalStateException("Invalid closing of generic type.");
            // register the generic token
            generics.add(token);
        }

        // check if the diamond operator was declared, but no generic types were given
        if (generics.isEmpty())
            throw new IllegalStateException("Generic declaration terminated before a type was given.");

        // handle the tokens of the generic types' declaration
        for (int i = 0; i < generics.size(); i++) {
            // get the type name of the generic type
            Token type = generics.get(i++);

            // handle generic tokens termination
            if (i == generics.size() - 1) {
                types.add(new GenericType(type.getValue(), null));
                break;
            }

            Token token = generics.get(i).expect(
                Token.of(TokenType.COMMA),
                Token.of(TokenType.OPERATOR, "=")
            );

            // handle the next generic type
            if (token.is(TokenType.COMMA)) {
                types.add(new GenericType(type.getValue(), null));
                continue;
            }

            // handle generic type default value
            // skip the '=' symbol
            if (++i == generics.size())
                throw new IllegalStateException("Generic declaration ended before default value was specified.");

            // get the default value of the generic type
            String defaultValue = generics.get(i)
                .expect(TokenType.IDENTIFIER, TokenType.TYPE)
                .getValue();

            // register the generic type with a default value given
            types.add(new GenericType(type.getValue(), defaultValue));

            // handle generic tokens termination
            if (i == generics.size() - 1)
                break;

            // handle the next generic type
            generics.get(++i).expect(TokenType.COMMA);
        }

        return new GenericTypeList(types, true);
    }

    /**
     * Parse the next generic arguments declaration.
     * <pre> {@code
     *     Map<UUID, Pair<User, Status>>
     * } </pre>
     * @return next generic argument list
     */
    @NotNull
    private GenericArgumentList nextGenericArgumentList() {
        List<GenericArgument> arguments = new ArrayList<>();

        // check if no generic arguments were given
        if (!peek().is(TokenType.OPERATOR, "<"))
            return new GenericArgumentList(arguments, false);

        // skip the '<' symbol
        get();

        // handle generic arguments
        while (!peek().is(TokenType.OPERATOR, ">")) {
            // parse the next inner generic argument
            arguments.add(nextGenericArgument());
            // check if there are more generic arguments to be parsed
            if (peek().is(TokenType.COMMA))
                get();
                // not expecting more, exit loop
            else
                break;
        }

        // skip the '>' symbol
        get(TokenType.OPERATOR, ">");
        return new GenericArgumentList(arguments, true);
    }

    @NotNull
    private GenericArgument nextGenericArgument() {
        // parse the type of the generic argument
        Type type = nextType();
        // check if the generic argument does not have inner generic arguments
        if (!peek().is(TokenType.OPERATOR, "<"))
            return new GenericArgument(type, null);
        // skip the '<' symbol
        get();
        // handle generic argument inner arguments
        List<GenericArgument> members = new ArrayList<>();
        while (!peek().is(TokenType.OPERATOR, ">")) {
            // parse the next inner generic argument
            members.add(nextGenericArgument());
            // check if there are more inner generic arguments to be parsed
            if (peek().is(TokenType.COMMA))
                get();
            // not expecting more, exit loop
            else
                break;
        }
        // skip the '>' symbol
        get(TokenType.OPERATOR, ">");
        return new GenericArgument(type, members);
    }

    /**
     * Parse the next fully qualified name of a type.
     * <pre> {@code
     *     My.Class.Inner.Element
     * } </pre>
     * The parts of the type are connected with dots.
     * @return next fully qualified type
     */
    private QualifiedName nextQualifiedName() {
        List<Token> tokens = new ArrayList<>();

        // get the first part of the fully qualified type
        Token first = get(TokenType.TYPE, TokenType.IDENTIFIER);
        tokens.add(first);

        // return here if the first type is primitive, as primitives
        // cannot have nested members, therefore we can stop processing here
        // TODO maybe enable this, but I'm currently not so sure what rules
        //  I want for primitives, as Void supports methods on primitive types
        // if (first.is(TokenType.TYPE))
        //     return result;

        // check if there are more tokens to be parsed
        while (peek().is(TokenType.OPERATOR, ".")) {
            // skip the '.' symbol
            get();
            // parse the next token type
            Token token = get(TokenType.IDENTIFIER, TokenType.TYPE /* here again, TYPE might be removed */);
            tokens.add(token);
            // exit the loop if there aren't any type tokens left
            if (!peek().is(TokenType.OPERATOR, "."))
                break;
        }

        return new QualifiedName(tokens);
    }

    /**
     * Parse the next method declaration.
     * @return new method node
     */
    public Node nextMethod() {
        return null;
    }

    /**
     * Get the node at the current index.
     * @return currently parsed token
     */
    private Token peek() {
        return at(cursor);
    }

    /**
     * Get the node at the current index.
     * Check if the retrieved token does not match the given type.
     * @param type required token type
     * @return currently parsed token
     */
    private Token peek(TokenType type) {
        // get the current token
        Token token = peek();
        // check if the current token does not match the required type
        if (!token.is(type))
            throw new IllegalStateException("Invalid token. Expected " + type + ", but got " + token);
        return token;
    }

    /**
     * Get the node at the index.
     * Check if the retrieved token does not match any of the given types.
     * @param types required token types
     * @return currently parsed token
     */
    private Token peek(TokenType... types) {
        // get the current token
        Token token = peek();
        for (TokenType type : types) {
            if (token.is(type))
                return token;
        }
        throw new IllegalStateException("Invalid token. Expected " + Arrays.toString(types) + ", but got " + token);
    }

    /**
     * Get the token at the current index and move to the next position.
     * @return currently parsed token
     */
    private Token get() {
        return at(cursor++);
    }

    /**
     * Get the token at the current index and move to the next position.
     * Check if the retrieved token does not match the given type.
     * @param type required token type
     * @return currently parsed token
     */
    private Token get(TokenType type) {
        // get the current token and move the cursor
        Token token = get();
        // check if the current token does not match the required type
        if (!token.is(type))
            throw new IllegalStateException("Invalid token. Expected " + type + ", but got " + token);
        return token;
    }

    /**
     * Get the token at the current index and move to the next position.
     * Check if the retrieved token does not match the given type or value.
     * @param type required token type
     * @return currently parsed token
     */
    private Token get(TokenType type, String value) {
        // get the current token and move the cursor
        Token token = get();
        // check if the current token does not match the required type
        if (!token.is(type, value))
            throw new IllegalStateException("Invalid token. Expected " + Token.of(type, value) + ", but got " + token);
        return token;
    }

    /**
     * Get the token at the current index and move to the next position.
     * Check if the retrieved token does not match any of the given types.
     * @param types  required token types
     * @return currently parsed token
     */
    private Token get(TokenType... types) {
        // get the current token and move the cursor
        Token token = get();
        for (TokenType type : types) {
            if (token.is(type))
                return token;
        }
        throw new IllegalStateException("Invalid token. Expected " + Arrays.toString(types) + ", but got " + token);
    }

    /**
     * Move the cursor with the give amount.
     * @param amount cursor move amount
     */
    private void skip(int amount) {
        cursor += amount;
    }

    /**
     * Get the token at the given index.
     * @param index token data index
     * @return token at the index or null if not in bounds
     */
    private Token at(int index) {
        return has(index) ? tokens.get(index) : Token.of(TokenType.FINISH);
    }

    /**
     * Determine if the given index is in bounds of the data size.
     * @param index target index to check
     * @return true if the index is in bounds
     */
    private boolean has(int index) {
        return index >= 0 && index < tokens.size();
    }


}
