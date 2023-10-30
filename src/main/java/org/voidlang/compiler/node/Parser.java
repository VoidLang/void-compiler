package org.voidlang.compiler.node;

import dev.inventex.octa.console.ConsoleFormat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.voidlang.compiler.builder.Package;
import org.voidlang.compiler.node.array.ArrayAllocate;
import org.voidlang.compiler.node.array.ArrayLoad;
import org.voidlang.compiler.node.array.ArrayStore;
import org.voidlang.compiler.node.common.Empty;
import org.voidlang.compiler.node.common.Error;
import org.voidlang.compiler.node.common.Finish;
import org.voidlang.compiler.node.control.*;
import org.voidlang.compiler.node.element.*;
import org.voidlang.compiler.node.element.Class;
import org.voidlang.compiler.node.info.PackageImport;
import org.voidlang.compiler.node.info.PackageSet;
import org.voidlang.compiler.node.local.*;
import org.voidlang.compiler.node.memory.Free;
import org.voidlang.compiler.node.memory.Malloc;
import org.voidlang.compiler.node.method.MethodCall;
import org.voidlang.compiler.node.operator.*;
import org.voidlang.compiler.node.type.pointer.DereferencingAccessor;
import org.voidlang.compiler.node.type.pointer.ReferencedAccessor;
import org.voidlang.compiler.node.type.pointer.Referencing;
import org.voidlang.compiler.node.value.*;
import org.voidlang.compiler.node.type.array.Array;
import org.voidlang.compiler.node.type.array.Dimension;
import org.voidlang.compiler.node.type.core.LambdaType;
import org.voidlang.compiler.node.type.generic.GenericArgumentList;
import org.voidlang.compiler.node.type.QualifiedName;
import org.voidlang.compiler.node.type.core.ScalarType;
import org.voidlang.compiler.node.type.core.Type;
import org.voidlang.compiler.node.type.core.CompoundType;
import org.voidlang.compiler.node.type.generic.GenericArgument;
import org.voidlang.compiler.node.type.generic.GenericType;
import org.voidlang.compiler.node.type.generic.GenericTypeList;
import org.voidlang.compiler.node.type.modifier.ModifierBlock;
import org.voidlang.compiler.node.type.modifier.ModifierList;
import org.voidlang.compiler.node.type.name.CompoundName;
import org.voidlang.compiler.node.type.name.Name;
import org.voidlang.compiler.node.type.name.ScalarName;
import org.voidlang.compiler.node.type.named.MethodParameter;
import org.voidlang.compiler.node.type.named.NamedScalarType;
import org.voidlang.compiler.node.type.named.NamedType;
import org.voidlang.compiler.node.type.named.NamedTypeGroup;
import org.voidlang.compiler.node.type.parameter.LambdaParameter;
import org.voidlang.compiler.token.Token;
import org.voidlang.compiler.token.TokenType;
import org.voidlang.compiler.util.Prettier;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a parser that transforms raw tokens to instruction nodes.
 */
public class Parser {
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

        // handle method declaration with a lambda type that doesn't have an explicit return type
        else if (peek().is(TokenType.OPERATOR, "|"))
            return nextMethod();

        // handle unexpected token
        System.out.println(ConsoleFormat.RED + "Error (Next) " + peek());
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
        System.out.println(ConsoleFormat.BLUE + "package " + ConsoleFormat.GREEN + "\"" + name + '"');
        return new PackageSet(name);
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
        return new PackageImport(name);
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
            return new ModifierBlock(modifiers);
        }
        // handle normal modifier list
        System.out.println(String.join(" ", modifiers) + " ");
        return new ModifierList(modifiers);
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
        System.out.println(ConsoleFormat.RED + "Error (Type/Method) " + error);
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
        GenericTypeList generics = nextGenericTypes();

        System.out.print(ConsoleFormat.YELLOW + kind + " " + ConsoleFormat.BLUE + name);
        if (generics.isExplicit()) {
            String debug = generics.getGenerics().stream()
                .map(GenericType::toString)
                .collect(Collectors.joining(", "));
            System.out.print("<" + String.join(", ", debug) + ConsoleFormat.BLUE + ">");
        }

        // TODO generic type implementation (where T implements MyType)

        // handle type-specific body parsing
        return switch (kind) {
            case "class" -> nextClass(name, generics);
            case "struct" -> nextStruct(name, generics);
            case "enum" -> nextEnum(name, generics);
            case "union" -> nextUnion(name, generics);
            case "interface" -> nextInterface(name, generics);
            default -> {
                System.out.println(ConsoleFormat.RED + "Error (Type)");
                yield new Error();
            }
        };
    }

    /**
     * Parse the next class type declaration.
     * @param name type name
     * @param generics generic type list
     * @return new declared class
     */
    private Node nextClass(String name, GenericTypeList generics) {
        // handle type body begin
        get(TokenType.BEGIN);

        System.out.println(ConsoleFormat.LIGHT_GRAY + " {");

        // parse the body of the class
        Node.prettier.enterScope();
        List<Node> body = new ArrayList<>();
        while (!peek().is(TokenType.END))
            body.add(nextContent());
        Node.prettier.exitScope();

        // handle type body end
        get(TokenType.END);

        System.out.println(ConsoleFormat.LIGHT_GRAY + "}");

        // handle auto-inserted semicolon at the end or the body
        if (peek().is(TokenType.SEMICOLON, "auto"))
            get();

        return new Class(name, generics, body);
    }

    private Node nextStruct(String name, GenericTypeList generics) {
        // handle type body begin
        get(TokenType.BEGIN);

        System.out.println(ConsoleFormat.LIGHT_GRAY + " {");

        // parse the body of the class
        Node.prettier.enterScope();
        List<Node> body = new ArrayList<>();
        while (!peek().is(TokenType.END))
            body.add(nextContent());
        Node.prettier.exitScope();

        // handle type body end
        get(TokenType.END);

        System.out.println(ConsoleFormat.LIGHT_GRAY + "}");

        // handle auto-inserted semicolon at the end or the body
        if (peek().is(TokenType.SEMICOLON, "auto"))
            get();

        return new Struct(name, generics, body);
    }

    /**
     * Parse the next content of a type, which might be a nested type, a method or a field.
     * @return new declared type, method or field
     */
    private Node nextContent() {
        // handle nested type declaration
        if (peek().is(TokenType.EXPRESSION))
            return nextTypeDeclaration();

        // handle scalar type method or field declaration
        else if (peek().is(TokenType.TYPE, TokenType.IDENTIFIER) && at(cursor + 1).is(TokenType.IDENTIFIER)) {
            if (at(cursor + 2).is(TokenType.OPEN))
                return nextMethod();
            return nextField();
        }

        System.err.println(ConsoleFormat.RED + "Error (Content) " + peek());
        return new Error();
    }

    private Node nextEnum(String name, GenericTypeList generics) {
        return null;
    }

    private Node nextUnion(String name, GenericTypeList generics) {
        return null;
    }

    private Node nextInterface(String name, GenericTypeList generics) {
        return null;
    }

    private NamedType nextNamedType(boolean expectLambda) {
        return nextNamedTypeInternal(false, expectLambda);
    }

    private NamedType nextNamedType() {
        return nextNamedTypeInternal(false, true);
    }

    private NamedType nextNamedTypeNested(boolean expectLambda) {
        return nextNamedTypeInternal(true, expectLambda);
    }

    private NamedType nextNamedTypeInternal(boolean expectName, boolean expectLambda) {
        // handle type group
        if (peek().is(TokenType.OPEN))
            return nextNamedTypeGroup(expectLambda);
        // handle scalar type
        return nextNamedScalarType(expectName, expectLambda);
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
    private NamedTypeGroup nextNamedTypeGroup(boolean expectLambda) {
        List<NamedType> members = new ArrayList<>();
        // skip the '(' symbol
        get(TokenType.OPEN);

        while (!peek().is(TokenType.CLOSE)) {
            // parse the next member of the group
            members.add(nextNamedTypeNested(expectLambda));
            // continue parsing if there are more members expected
            if (peek(TokenType.COMMA, TokenType.CLOSE).is(TokenType.COMMA))
                get();
            // stop parsing if the group has been closed
            else
                break;
        }

        // skip the ')' symbol
        get(TokenType.CLOSE);

        // TODO parse referencing
        return new NamedTypeGroup(Referencing.none(), members);
    }

    private Type nextLambdaType(Type returnType) {
        // skip the '|' symbol
        get(TokenType.OPERATOR, "|");
        // parse the parameter list of the lambda
        List<LambdaParameter> parameters = new ArrayList<>();
        while (!peek().is(TokenType.OPERATOR, "|")) {
            // parse the next lambda parameter type
            Type type = nextNamedType(false);
            // parse the variadic arguments specifier of the type
            boolean variadic = nextVarargs();

            // check if the lambda parameter does not have a type specified
            if (peek().is(TokenType.COMMA) || peek().is(TokenType.OPERATOR, "|")) {
                // register an unnamed lambda parameter
                // TODO parse referencing
                parameters.add(new LambdaParameter(Referencing.none(), type, variadic, null, false));
                // skip the ',' symbol
                if (peek().is(TokenType.COMMA))
                    get();
                // continue handling parameters, or exit because of the condition
                continue;
            }

            // parse the name of the lambda parameter type
            Name name = nextName();
            // register a named lambda parameter
            // TODO parse referencing
            parameters.add(new LambdaParameter(Referencing.none(), type, variadic, name, true));

            // check if there are more parameters to be parsed
            if (peek().is(TokenType.COMMA))
                get();
            // lambda parameter declaration ended, exit the loop
            else
                break;
        }
        // skip the '|' symbol
        get(TokenType.OPERATOR, "|");
        // TODO parse referencing
        return new LambdaType(Referencing.none(), returnType, parameters);
    }

    private NamedScalarType nextNamedScalarType(boolean expectName, boolean expectLambda) {
        // parse the type of the named type
        Type type = nextScalarType(expectLambda);
        // check if a name is declared for the type
        String name = "";
        if (expectName && peek().is(TokenType.IDENTIFIER))
            name = get().getValue();
        // handle unnamed scalar type
        // TODO parse referencing
        return new NamedScalarType(Referencing.none(), type, name, !name.isEmpty());
    }

    private Type nextType() {
        return nextType(true);
    }

    private boolean nextMutable() {
        if (peek().is(TokenType.TYPE, "mut")) {
            get();
            return true;
        }
        return false;
    }

    private Type nextType(boolean expectLambda) {
        // handle type group
        if (peek().is(TokenType.OPEN))
            return nextTypeGroup();
        // handle scalar type
        return nextScalarType(expectLambda);
    }

    private CompoundType nextTypeGroup() {
        List<Type> members = new ArrayList<>();
        // skip the '(' symbol
        get(TokenType.OPEN);

        while (!peek().is(TokenType.CLOSE)) {
            // parse the next member of the group
            Type member = nextType();
            members.add(member);
            // continue parsing if there are more members expected
            if (peek(TokenType.COMMA, TokenType.CLOSE).is(TokenType.COMMA))
                get();
                // stop parsing if the group has been closed
            else
                break;
        }

        // skip the ')' symbol
        get(TokenType.CLOSE);

        // TODO parse referencing
        return new CompoundType(Referencing.none(), members);
    }

    private Type nextScalarType(boolean expectLambda) {
        // check for lambda type declaration without an explicit return type
        if (peek().is(TokenType.OPERATOR, "|"))
            return nextLambdaType(Type.primitive("void"));

        // parse the referencing of the type
        // mut int x
        // ^^^ 'mut' indicates, that 'x' can be mutated
        // ref int y
        // ^^^ 'ref' indicates, that 'y' should be taken as a pointer
        // ref* int z
        //    ^ '*' indicates, that 'z' should be taken as a pointer to a pointer
        Referencing referencing = nextReferencing();

        // parse the fully qualified name of the type
        // User.Type getUserType()
        // ^^^^^^^^^ the tokens joined with the '.' operator are the specifiers of the type
        QualifiedName name = nextQualifiedName();

        // parse the generic arguments of the type
        // List<Element> myList
        //     ^^^^^^^^^ the tokens between angle brackets are the generic arguments of the type
        GenericArgumentList generics = nextGenericArgumentList();

        // check if generic arguments were declared for a primitive type
        // TODO if I ever disabled this check, I should modify nextQualifiedName() as well
        if (name.isPrimitive() && generics.isExplicit())
            throw new IllegalStateException("Primitive types cannot have generic type arguments.");

        // parse the array dimensions of the type
        Array array = nextArray();

        // create the type wrapper
        ScalarType type = new ScalarType(referencing, name, generics, array);

        // check if a lambda parameter list declaration is after the type
        // do not handle '|' if we are currently parsing a lambda
        if (expectLambda && peek().is(TokenType.OPERATOR, "|"))
            return nextLambdaType(type);

        // handle scalar type
        return type;
    }

    /**
     * Parse the next variadic type specified declaration.
     * @return true if the previous type was variadic
     */
    private boolean nextVarargs() {
        // check if the previous type wasn't variadic
        if (!peek().is(TokenType.OPERATOR, "."))
            return false;
        // skip the variadic type specifier
        for (int i = 0; i < 3; i++)
            get(TokenType.OPERATOR, ".");
        return true;
    }

    private Name nextName() {
        // handle compound name
        if (peek().is(TokenType.OPEN))
            return nextCompoundName();
        // handle scalar name
        return nextScalarName();
    }

    private Name nextScalarName() {
        return new ScalarName(get(TokenType.IDENTIFIER).getValue());
    }

    private Name nextCompoundName() {
        // skip the '(' symbol
        get(TokenType.OPEN);
        List<Name> members = new ArrayList<>();
        while (!peek().is(TokenType.CLOSE)) {
            // parse the next member of the group
            members.add(nextName());
            // continue parsing if there are more members expected
            if (peek(TokenType.COMMA, TokenType.CLOSE).is(TokenType.COMMA))
                get();
            // stop parsing if the group has been closed
            else
                break;
        }
        // skip the ')' symbol
        get(TokenType.CLOSE);
        return new CompoundName(members);
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
        // skip the '<' symbol
        get();
        while (!peek().is(TokenType.OPERATOR, ">")) {
            // parse the next generic type
            types.add(nextGenericType());
            // check if there are more generic types to be parsed
            if (peek().is(TokenType.COMMA))
                get();
            // generic type declaration ended, exit loop
            else
                break;
        }
        // skip the '>' symbol
        get(TokenType.OPERATOR, ">");
        return new GenericTypeList(types ,true);
    }

    private GenericType nextGenericType() {
        // get the type name of the generic type
        String name = get(TokenType.IDENTIFIER).getValue();
        // return if the generic type does not have a default value
        if (!peek().is(TokenType.OPERATOR, "="))
            return new GenericType(name, null);
        // skip the '=' symbol
        get(TokenType.OPERATOR, "=");
        // get the default value of the generic type
        return new GenericType(name, nextNamedType());
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
        List<GenericArgument> members = new ArrayList<>();
        if (!peek().is(TokenType.OPERATOR, "<"))
            return new GenericArgument(type, members, false);
        // skip the '<' symbol
        get();
        // handle generic argument inner arguments
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
        return new GenericArgument(type, members, true);
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
        // TODO maybe disable this, but I'm currently not so sure what rules
        //  I want for primitives, as Void supports methods on primitive types
        if (first.is(TokenType.TYPE))
            return new QualifiedName(tokens);

        // check if there are more tokens to be parsed
        while (peek().is(TokenType.OPERATOR, ".")) {
            // skip the '.' symbol
            get();
            // parse the next token type
            Token token = get(
                TokenType.IDENTIFIER,
                TokenType.TYPE, // here again, TYPE might be removed
                TokenType.INTEGER // support tuple indexing
            );
            tokens.add(token);
            // exit the loop if there aren't any type tokens left
            if (!peek().is(TokenType.OPERATOR, "."))
                break;
        }

        return new QualifiedName(tokens);
    }

    /**
     * Parse the next array declaration.
     * @return next type array
     */
    private Array nextArray() {
        // loop while the token is an array start
        // int[] myArray
        //    ^  square brackets indicate that the type is an array
        // float[][] my2DArray
        //      ^ ^ multiple square brackets indicate the dimensions of an array
        //           this one is a 2-dimensional array for example
        // byte[1024] a; byte[BUFFER_SIZE] b
        //     ^^^^^          ^^^^^^^^^^^  array size may be explicitly declared with an integer
        //                                 or an identifier referring to a constant
        List<Dimension> dimensions = new ArrayList<>();
        while (peek().is(TokenType.START)) {
            // skip the '[' symbol
            get();
            // handle explicitly declared array dimension size
            Token size = Token.of(TokenType.NONE);
            if (peek().is(TokenType.INTEGER, TokenType.IDENTIFIER))
                size = get();
            // float[] getVectorElements()
            //       ^ a closing square bracket must be placed right after an open square bracket
            get(TokenType.STOP);
            dimensions.add(new Dimension(size, !size.is(TokenType.NONE)));
        }
        return new Array(dimensions);
    }

    /**
     * Parse the next referencing of a type.
     * @return next type referencing
     */
    private Referencing nextReferencing() {
        // handle mutable type referencing
        if (peek().is(TokenType.TYPE, "mut")) {
            get();
            return Referencing.mutable();
        }

        // handle pointer referencing or dereferencing
        else if (peek().is(TokenType.TYPE)) {
            Token token = peek();
            if (!token.val("ref", "deref"))
                return Referencing.none();
            get();

            int dimensions = 1;
            while (peek().is(TokenType.OPERATOR, "*")) {
                get();
                dimensions++;
            }

            return token.val("ref")
                ? Referencing.reference(dimensions)
                : Referencing.dereference(dimensions);
        }

        // handle default referencing
        return Referencing.none();
    }

    /**
     * Parse the next method declaration.
     * @return new method node
     */
    public Node nextMethod() {
        // parse the type of the method
        // int getUserBalance(string user)
        // ^^^ the method has only one return type, "int"
        // (int, string) fetchURL(String url)
        // ^           ^  multi-return types are placed in between parenthesis
        // (bool code, string message) authenticate(String username, String password)
        //       ^^^^         ^^^^^^^ you can even name these return types
        NamedType type = nextNamedType();

        // parse the name of the method
        // void greet(string person) { println($"Hi, {person}") }
        //      ^^^^^ the identifier after the type token(s) is the name of the method
        String name = get(TokenType.IDENTIFIER).getValue();

        // parse the generic types of the method
        // in here we only define what identifiers we are willing to use as generic types inside the method
        // void concat<T>(List<T> firstList, List<T> secondList)
        //            ^^^ method generics are placed after the method name
        // void createMap<K,V>()
        //                 ^ you may have multiple method generic types
        //                   they are also separated with a comma
        // T serialize<T = JsonObject>(string json)
        //               ^^^^^^^^^^^^^ generic types may have a default value specified
        GenericTypeList genericTypes = nextGenericTypes();

        // handle method parameter list
        // int multiply(int i, int j)
        //             ^ open parenthesis indicates, that the declaration of the parameter list has begun
        // skip the '(' symbol as it is already handled
        get(TokenType.OPEN);

        Node.prettier.indent();
        System.out.print(type + " " + ConsoleFormat.BLUE + name + genericTypes + ConsoleFormat.CYAN + '(');

        // parse the method parameters
        List<MethodParameter> parameters = new ArrayList<>();
        while (!peek().is(TokenType.CLOSE)) {
            // parse the next parameter type
            Type paramType = nextType();

            boolean paramVar = nextVarargs();

            Name paramName = nextName();

            MethodParameter parameter = new MethodParameter(paramType, paramVar, paramName);

            parameters.add(parameter);
            System.out.print(parameter);

            // check if there are more parameters to be parsed
            if (peek().is(TokenType.COMMA)) {
                get();
                System.out.print(ConsoleFormat.CYAN + ", ");
            }
            // no more parameters expected, exit the loop
            else
                break;
        }

        get(TokenType.CLOSE);

        System.out.println(ConsoleFormat.CYAN + ") " + ConsoleFormat.DARK_GRAY + "{");

        Token peek = peek();
        if (peek.is(TokenType.SEMICOLON)) {
            // skip the auto-inserted semicolon before the method body
            get();

            // handle methods without an explicit body
            // TODO make sure these methods have an extern modifier
            if (!peek().is(TokenType.BEGIN)) {
                Method method = new Method(type, name, parameters, new ArrayList<>());
                method.setBodyLess(true);
                return method;
            }
        }

        if (peek().is(TokenType.SEMICOLON, "auto"))
            get();

        // TODO handle direct body assigning to method
        //  int isFruit(Produce produce) = switch (produce) { BANANA|APPLE -> true; else -> false }
        // TODO handle constant value getter
        //  int getName() => "John, Doe" | or ->, haven't decided yet

        // handle method body begin
        get(TokenType.BEGIN);

        List<Node> body = new ArrayList<>();
        while (!peek().is(TokenType.END)) {
            Node expression = nextExpression();
            if (!expression.hasNext())
                break;
            if (!(expression instanceof Empty))
                body.add(expression);
        }

        Prettier prettier = Node.prettier;
        prettier.enterScope();
        for (Node node : body) {
            prettier.indent();
            prettier.begin(node);
            prettier.content(node);
            prettier.end();
        }
        prettier.exitScope();

        Node.prettier.indent();
        System.out.println(ConsoleFormat.DARK_GRAY + "}");

        // handle method body end
        get(TokenType.END);

        // skip the auto-inserted semicolon
        if (peek().is(TokenType.SEMICOLON))
            get();

        return new Method(type, name, parameters, body);
    }

    private Node nextField() {
        // parse the type of the field
        NamedType type = nextNamedType();

        // get the name of the field
        String name = get(TokenType.IDENTIFIER).getValue();

        // handle field without an explicit default value
        if (peek().is(TokenType.SEMICOLON)) {
            get();
            Node.prettier.indent();
            System.out.print(type + " " + ConsoleFormat.BLUE + name);
            System.out.println();
            return new Field(type, name, null);
        }

        // handle multi-field declaration
        else if (peek().is(TokenType.COMMA))
            return nextMultiField(type, name, null);

        // handle field value assignation
        get(TokenType.OPERATOR, "=");

        // parse the value of the field
        Node value = nextExpression();

        // handle multi-field declaration
        if (peek().is(TokenType.COMMA))
            return nextMultiField(type, name, value);

        // skip the semicolon after the field declaration
        get(TokenType.SEMICOLON);

        Node.prettier.indent();
        System.out.print(type + " " + ConsoleFormat.BLUE + name);
        System.out.print(ConsoleFormat.CYAN + " = ");
        Node.prettier.processValue(value);

        return new Field(type, name, value);
    }

    private Node nextMultiField(Type type, String name, @Nullable Node value) {
        // skip the ',' symbol
        get(TokenType.COMMA);

        Node.prettier.indent();
        System.out.println(type);

        Node.prettier.enterScope();
        Node.prettier.indent();
        System.out.print(ConsoleFormat.BLUE + name);

        // create a map for the fields that keep the order they've been declared at
        Map<String, Node> values = new LinkedHashMap<>();
        values.put(name, value);

        if (value != null) {
            System.out.print(ConsoleFormat.CYAN + " = ");
            Node.prettier.processValue(value);
        }
        else
            System.out.println();

        while (has(cursor)) {
            // parse the name of the field
            String fieldName = get(TokenType.IDENTIFIER).getValue();

            Node.prettier.indent();
            System.out.print(ConsoleFormat.BLUE + fieldName);

            // parse the value of the field
            Node fieldValue = null;
            if (peek().is(TokenType.OPERATOR, "=")) {
                get();
                fieldValue = nextExpression();
                System.out.print(ConsoleFormat.CYAN + " = ");
                Node.prettier.processValue(fieldValue);
            }
            else
                System.out.println();

            // register the field
            // TODO error if the field name is already in the map
            values.put(fieldName, fieldValue);

            // check for more fields
            if (peek().is(TokenType.COMMA))
                get();

            // check if the multi-field declaration has been ended
            else if (peek().is(TokenType.SEMICOLON))
                break;
        }

        Node.prettier.exitScope();

        get(TokenType.SEMICOLON);

        return new MultiField(type, values);
    }

    private Value nextValue() {
        return nextValue(false);
    }

    private Value nextValue(boolean ignoreJoin) {
        if (peek().is(TokenType.TYPE, "let"))
            return nextImmutableLocalDeclaration();

        else if (peek().is(TokenType.TYPE, "mut"))
            return nextMutableLocalDeclaration();

        else if (peek().is(TokenType.TYPE, "ref"))
            return nextReferenceLocalDeclaration();

        // handle variable assignation
        if (peek().is(TokenType.IDENTIFIER) && at(cursor + 1).is(TokenType.OPERATOR, "=")
                && !at(cursor + 2).is(TokenType.OPERATOR, "="))
            return nextLocalAssignation();

        // handle node grouping
        // let a = (b + c) + d
        //         ^ the open parenthesis indicate, that the following nodes should be placed in a node group
        else if (peek().is(TokenType.OPEN))
            return nextGroupOrTuple(ignoreJoin);

        // handle literal constant value
        // let name = "John Doe"
        //            ^^^^^^^^^^ the literal token indicates, that a value is expected
        else if (peek().isLiteral())
            return nextLiteral();

        // handle value referencing
        else if (peek().is(TokenType.TYPE, "ref"))
            return nextReferencedQualifiedNameOrCall();

        // handle pointer dereferencing
        else if (peek().is(TokenType.TYPE, "deref"))
            return nextDereferencedQualifiedNameOrCall();

        // handle qualified name or method call
        else if (peek().is(TokenType.IDENTIFIER))
            return nextQualifiedNameOrCall();

        // handle new statement
        else if (peek().is(TokenType.EXPRESSION, "new"))
            return nextNewType(ignoreJoin);

        // handle single-node operation
        if (peek().is(TokenType.OPERATOR)) {
            Operator operator = nextOperator();
            if (!isSideOperator(operator.getValue()))
                throw new IllegalStateException("Expected side operator, but received " + operator);
            return new SideOperation(operator, nextValue());
        }

        // handle array allocation
        else if (peek().is(TokenType.START))
            return nextArrayAllocation();

        // handle "sizeof" operator
        else if (peek().is(TokenType.EXPRESSION, "sizeof")) {
            get();
            return new Sizeof(nextType());
        }

        // handle "default" keyword
        else if (peek().is(TokenType.MODIFIER, "default")) {
            get();
            get(TokenType.OPEN);
            Type type = nextType();
            get(TokenType.CLOSE);
            return new Default(type);
        }

        // TODO handle "typeof" operator

        // handle allocation on the heap using "malloc"
        else if (peek().is(TokenType.EXPRESSION, "malloc"))
            return nextMalloc();

        // handle heap deallocation using "free"
        else if (peek().is(TokenType.EXPRESSION, "free"))
            return nextFree();

        System.err.println(ConsoleFormat.RED + "Error (Value) " + peek());
        return new Error();
    }

    private Value nextArrayAllocation() {
        // skip the '[' symbol
        get(TokenType.START);

        List<Value> values = new ArrayList<>();
        while (!peek().is(TokenType.STOP)) {
            // parse the next value
            values.add(nextValue());
            // check if there are more values to be parsed
            if (peek().is(TokenType.COMMA))
                get();
            // no more values expected, exit loop
            else
                break;
        }

        // skip the ']' symbol
        get(TokenType.STOP);

        return new ArrayAllocate(values);
    }

    private Node nextExpression() {
        return nextExpression(false);
    }

    /**
     * Parse the next expression instruction.
     * @param ignoreJoin TODO
     * @return expression
     */
    private Node nextExpression(boolean ignoreJoin) {
        // handle return statement
        if (peek().is(TokenType.EXPRESSION, "return"))
            return nextReturnStatement();

        // handle if statement
        else if (peek().is(TokenType.EXPRESSION, "if"))
            return nextIfStatement();

        // handle while statement
        else if (peek().is(TokenType.EXPRESSION, "while"))
            return nextWhileStatement();

        // handle do while statement
        else if (peek().is(TokenType.EXPRESSION, "do"))
            return nextDoWhileStatement();

        // ignore unexpected auto-inserted semicolon
        else if (peek().is(TokenType.SEMICOLON, "auto")) {
            get();
            return new Empty();
        }

        return nextValue(ignoreJoin);
    }

    private Value nextMalloc() {
        // skip the "malloc" keyword
        get(TokenType.EXPRESSION, "malloc");

        // parse the name of the target type
        QualifiedName name = nextQualifiedName();

        Value node = new Malloc(name);

        if (peek().is(TokenType.SEMICOLON))
            get();

        return node;
    }

    private Value nextFree() {
        get(TokenType.EXPRESSION, "free");

        // parse the name of the target type
        QualifiedName name = nextQualifiedName();

        Value node = new Free(name);

        if (peek().is(TokenType.SEMICOLON))
            get();

        return node;
    }

    private Value nextNewType(boolean ignoreJoin) {
        // skip the "new" keyword
        get(TokenType.EXPRESSION, "new");

        // parse the name of the target type
        QualifiedName name = nextQualifiedName();

        // check if the "new" keyword has an argument list
        List<Value> arguments = new ArrayList<>();
        if (peek().is(TokenType.OPEN))
            arguments = nextArgumentList();

        // check if the "new" keyword has an initializator
        Initializator initializator = null;
        if (peek().is(TokenType.BEGIN))
            initializator = nextInitializator();

        Value node = new New(name, arguments, initializator);

        // TODO handle normal and join operator

        // check if the method call is used as a statement or isn't expecting to be passed in a nested context
        // let result = new Foo("my input");
        //                                 ^ the semicolon indicates, that the method call does not have any
        //                                   expressions after. unlike: let res = foo() + bar
        //                                   let test = new Foo(); <- method call value is terminated, not expecting anything afterward
        if (peek().is(TokenType.SEMICOLON))
            get();

        return node;
    }

    /**
     * Parse the next structure initializator declaration.
     * @return new structure initializator
     */
    private Initializator nextInitializator() {
        // skip the '{' symbol
        get(TokenType.BEGIN);

        // parse the members of the initializator
        Map<String, Node> members = new LinkedHashMap<>();
        while (!peek().is(TokenType.END)) {
            // parse the key of the member
            String key = get(TokenType.IDENTIFIER).getValue();

            // handle the separator ':' symbol of the key-value pair
            get(TokenType.COLON);

            // parse the value of the member
            Node value;
            // check if the value is also an initializer
            if (peek().is(TokenType.BEGIN))
                value = nextInitializator();
            // handle regular initializator value
            else
                value = nextValue();

            // register the initializator member
            // TODO warn for duplicate members
            members.put(key, value);

            // handle auto-inserted semicolon
            if (peek().is(TokenType.SEMICOLON, "auto"))
                get();

            // check if there are more members yet to be parsed
            if (peek().is(TokenType.COMMA))
                get();
            // no more initializator members
            else
                break;
        }

        // skip the '}' symbol
        get(TokenType.END);

        return new Initializator(members);
    }

    /**
     * Parse the next while loop statement declaration.
     * @return new while statement
     */
    private Node nextWhileStatement() {
        // skip the "while" keyword
        get(TokenType.EXPRESSION, "while");

        // parse the condition of the while statement
        Node condition = nextCondition();

        // handle while statement without an explicit body
        // tbh, I'm not quite sure why is this allowed in so many languages, but I'll just support doing it
        if (peek().is(TokenType.SEMICOLON)) {
            get();
            return new While(condition, new ArrayList<>());
        }

        // parse the body of the while statement
        List<Node> body = nextStatementBody();
        return new While(condition, body);
    }

    /**
     * Parse the next do-while statement declaration.
     */
    private Node nextDoWhileStatement() {
        // skip the "do" keyword
        get(TokenType.EXPRESSION, "do");

        // parse the body of the do-while statement
        List<Node> body = nextStatementBody();

        // skip the "while" keyword
        get(TokenType.EXPRESSION, "while");

        // parse the condition of the do-while statement
        Node condition = nextCondition();
        return new DoWhile(body, condition);
    }

    /**
     * Parse the next if statement declaration.
     * @return new if statement
     */
    private Node nextIfStatement() {
        // skip the "if" keyword
        get(TokenType.EXPRESSION, "if");

        // parse the statement condition
        Node condition = nextCondition();

        // handle if statement without an explicit body
        // tbh, I'm not quite sure why is this allowed in so many languages, but I'll just support doing it
        if (peek().is(TokenType.SEMICOLON)) {
            get();
            return new If(condition, new ArrayList<>());
        }

        // parse the body of the if statement
        If statement = new If(condition, nextStatementBody());

        // handle else or else if cases
        if (peek().is(TokenType.EXPRESSION, "else")) {
            // handle else if cases
            if (at(cursor + 1).is(TokenType.EXPRESSION, "if")) {
                // parse the next else if statements
                while (peek().is(TokenType.EXPRESSION, "else")
                        && at(cursor + 1).is(TokenType.EXPRESSION, "if"))
                    statement.getElseIfs().add((ElseIf) nextElseIfStatement());
            }
            // check if an else case still follows
            // maybe there were else cases before
            else if (peek().is(TokenType.EXPRESSION, "else"))
                statement.setElseCase((Else) nextElseStatement());
        }

        return statement;
    }

    /**
     * Parse the next else if statement declaration.
     * @return new else if statement
     */
    private Node nextElseIfStatement() {
        // skip the "else" keyword
        get(TokenType.EXPRESSION, "else");
        // skip the "if" keyword
        get(TokenType.EXPRESSION, "if");

        // parse the statement condition
        Node condition = nextCondition();

        // handle else if statement without an explicit body
        // tbh, I'm not quite sure why is this allowed in so many languages, but I'll just support doing it
        if (peek().is(TokenType.SEMICOLON)) {
            get();
            return new ElseIf(condition, new ArrayList<>());
        }

        // parse the body of the else if statement
        List<Node> body = nextStatementBody();
        return new ElseIf(condition, body);
    }

    /**
     * Parse the next else statement declaration.
     * @return new else statement
     */
    Node nextElseStatement() {
        // skip the "else" keyword
        get(TokenType.EXPRESSION, "else");

        // parse the body of the else statement
        List<Node> body = nextStatementBody();
        return new Else(body);
    }

    /**
     * Parse the next block of instructions that belong to a block, such as if, else if, while.
     * @return new block statement body
     */
    private List<Node> nextStatementBody() {
        // parse the body of the statement
        List<Node> body = new ArrayList<>();

        // check if multiple instructions should be assigned for the body
        // <expression> (condition) { /* do something */ }
        //                   ^ the open curly bracket indicates, that the statement body has multiple instructions inside
        if (peek().is(TokenType.BEGIN)) {
            get(TokenType.BEGIN);
            // parse the while statement instructions
            while (!peek().is(TokenType.END))
                body.add(nextExpression());
            get(TokenType.END);
        }

        // handle single-instruction statement
        // <expression> (condition) foo()
        //                          ^ if there is no open curly bracket after the condition, it means
        //                            that there is only one instruction for the statement body
        else /* there is no '{' after the condition */
            body.add(nextExpression());

        // skip the auto-inserted semicolon after  statement body
        // TODO might want to ignore manually inserted semicolon as well
        if (peek().is(TokenType.SEMICOLON, "auto"))
            get();

        return body;
    }

    /**
     * Parse the next condition of a condition block, such as if, else if, while.
     * @return new conditional node
     */
    private Node nextCondition() {
        // handle the beginning of the condition
        get(TokenType.OPEN);

        // parse the statement condition
        // TODO support conditional let, instanceof simplifier, pattern matching
        Node condition = nextExpression();

        // handle the ending of the condition
        get(TokenType.CLOSE);

        // handle auto-inserted semicolon after condition
        if (peek().is(TokenType.SEMICOLON, "auto")) // make sure to only handle auto-inserted semicolons here, as manually inserting
            get();                                        // one would mean the statement has no statement body
                                                          // <expression> (condition); outer();
                                                          //                         ^ statement terminated here
        return condition;
    }

    /**
     * Parse the next group or tuple declaration.
     * @return new group or tuple
     */
    private Value nextGroupOrTuple(boolean ignoreJoin) {
        // let a = (b + c) + d
        //         ^ the open parenthesis indicate, that the following nodes should be placed in a node group
        // skip the '(' sign
        get(TokenType.OPEN);

        // parse the expression inside the group
        // let res = (1 + 2 + 3) / 4
        //            ^^^^^^^^^ the nodes between parenthesis are the content of the node group
        Value value = nextValue();

        // handle tuple declaration
        // let tup = (1, 2, 3)
        //             ^ the comma after the first member indicates, that this is a tuple declaration
        if (peek().is(TokenType.COMMA)) {
            // register the first member of the tuple
            List<Value> members = new ArrayList<>();
            members.add(value);
            // skip the ',' symbol
            get();

            // parse the remaining members of the tuple
            while (!peek().is(TokenType.CLOSE)) {
                // parse the next member of the tuple
                members.add(nextValue());

                // check if there are more members to be parsed
                if (peek().is(TokenType.COMMA))
                    get();
                // no more elements to be parsed, exit loop
                else
                    break;
            }

            // handle tuple ending
            get(TokenType.CLOSE);

            return new Tuple(members);
        }

        // handle the group closing
        // let test = (7 - 1)
        //                  ^ the closing parenthesis indicate, that the declaration of node group has been ended
        get(TokenType.CLOSE);

        // warp the value around a group node, therefore the operation tree transformer
        // will correctly parse precedence
        Group group = new Group(value);

        // handle operation after a node group
        // (2 + 3) + 7
        //         ^ the operator indicates, that the method call should be grouped with the expression afterward
        if (peek().is(TokenType.OPERATOR)) {
            // parse the operator of the operation
            Operator operator = nextOperator();
            if (operator == Operator.QUESTION)
                return nextSelection(group);
            else if (!isComplexOperator(operator.getValue()))
                throw new IllegalStateException("Expected complex operator, but got " + operator);
            return makeOperator(group, operator, nextValue());
        }

        // handle type casting
        // let val = 100 as float
        //                 ^ the 'as' keyword indicates, that the expression has been terminated
        else if (peek().is(TokenType.EXPRESSION, "as")) {
            get();
            return new Casting(group, nextType());
        }

        return group;
    }

    /**
     * Parse the next value return statement declaration.
     * @return new return statement
     */
    private Node nextReturnStatement() {
        // skip the "return" keyword
        get(TokenType.EXPRESSION, "return");

        // check if the return statement has no value to return
        if (peek().is(TokenType.SEMICOLON)) {
            get();
            return new Return(null);
        }

        // parse the value to be returned
        Value value = nextValue();

        // handle the semicolon after the return statement
        if (peek().is(TokenType.SEMICOLON))
            get();

        return new Return(value);
    }

    /**
     * Parse the next local variable value assignation.
     * @return new local assignation
     */
    private Value nextLocalAssignation() {
        // get the name of the local variable
        String name = get().getValue();

        // skip the equals sign
        get(TokenType.OPERATOR, "=");

        // parse the value of the local variable
        Value value = nextValue();

        // skip the semicolon after the declaration
        if (peek().is(TokenType.SEMICOLON))
            get();

        return new LocalAssign(name, value);
    }

    private Value nextSelection(Value condition) {
        Value ifCase = nextValue();

        get(TokenType.COLON);

        Value elseCase = nextValue();

        return new Selection(condition, ifCase, elseCase);
    }

    /**
     * Parse the next literal value declaration.
     * @return new literal
     */
    private Value nextLiteral() {
        // handle literal constant or identifier
        //
        // let name = "John Doe"
        //            ^^^^^^^^^^ the literal token indicates, that a value is expected
        Token token = get(
            TokenType.BOOLEAN, TokenType.CHARACTER, TokenType.STRING,
            TokenType.BYTE, TokenType.UBYTE, TokenType.SHORT, TokenType.USHORT,
            TokenType.INTEGER, TokenType.UINTEGER, TokenType.LONG, TokenType.ULONG,
            TokenType.FLOAT, TokenType.DOUBLE,
            TokenType.HEXADECIMAL, TokenType.BINARY
        );
        Literal literal = new Literal(token);

        // handle single value expression, in which case the local variable is initialized with a single value
        // let myVar = 100;
        //                ^ the (auto-inserted) semicolon indicates, initialized with a single value
        if (peek().is(TokenType.SEMICOLON))
            return literal;

        // terminate the literal if an 'else' case of a one-liner 'if' statement is expected
        // let foo = x < 10 ? 1 + 2 : 12 / 6
        //                         ^ terminate the parsing of '1 + 2', as the else case is expected
        if (peek().is(TokenType.COLON))
            return literal;

        // handle operation between two expressions
        // let var = 100 +
        //               ^ the operator after a literal indicates, that there are more expressions to be parsed
        //                 the two operands are grouped together by an Operation node
        if (peek().is(TokenType.OPERATOR)) {
            // parse the operator of the operation
            Operator operator = nextOperator();
            if (operator == Operator.QUESTION)
                return nextSelection(literal);
            else if (!isComplexOperator(operator.getValue()))
                throw new IllegalStateException("Expected complex operator, but got " + operator);
            return makeOperator(literal, operator, nextValue());
        }

        // TODO handle close, comma, stop, end

        // handle group closing
        // let val = (1 + 2) / 3
        //                 ^ the close parenthesis indicates, that we are not expecting any value after the current token
        else if (peek().is(TokenType.CLOSE))
            return literal;

        // handle argument list or array fill
        // foo(123, 450.7)
        //        ^ the comma indicates, that the expression has been terminated
        else if (peek().is(TokenType.COMMA))
            return literal;

        // handle index closing or array end
        // foo[10] = 404
        //       ^ the closing square bracket indicates, that the expression has been terminated
        else if (peek().is(TokenType.STOP))
            return literal;

        // handle initializator end
        // new Pair { key: "value" }
        //                         ^ the closing bracket indicates, that the initializator has been terminated
        else if (peek().is(TokenType.END))
            return literal;

        // handle type casting
        // let val = 100 as float
        //                 ^ the 'as' keyword indicates, that the expression has been terminated
        else if (peek().is(TokenType.EXPRESSION, "as")) {
            get();
            return new Casting(literal, nextType());
        }

        // TODO handle indexing

        System.out.println(ConsoleFormat.RED + "Error (Literal) " + peek());
        return new Error();
    }

    private Value nextDereferencedQualifiedNameOrCall() {
        get(TokenType.TYPE, "deref");

        // parse the qualified name
        QualifiedName name = nextQualifiedName();

        Value value = new DereferencingAccessor(name);

        // handle method call
        // println("Hello, World!")
        //        ^ the open parenthesis token after an identifier indicates, that a method call is expected
        if (peek().is(TokenType.OPEN))
            throw new IllegalStateException("Dereferenced method call is not supported yet.");

        // handle group closing
        // print(ref foo)
        //              ^ we don't need to handle this closing tag here, just finish qualified name parsing
        if (peek().is(TokenType.CLOSE, TokenType.COMMA, TokenType.STOP, TokenType.END))
            return value;

        // handle single value expression, in which case the local variable is initialized with a single value
        // let myVar = ref foo;
        //                    ^ the (auto-inserted) semicolon indicates, initialized with a single value
        if (peek().is(TokenType.SEMICOLON))
            return value;

        // terminate the literal if an 'else' case of a one-liner 'if' statement is expected
        // let foo = x < 10 ? 1 + 2 : 12 / 6
        //                         ^ terminate the parsing of '1 + 2', as the else case is expected
        if (peek().is(TokenType.COLON))
            return value;

        // handle operation between two expressions
        // let var = foo +
        //               ^ the operator after an identifier indicates, that there are more expressions to be parsed
        //                 the two operands are grouped together by an Operation node
        if (peek().is(TokenType.OPERATOR))
            throw new IllegalStateException("Dereferenced operation is not supported yet.");

        System.out.println(ConsoleFormat.RED + "Error (Dereferenced Qualified Name / Call) " + peek());
        return new Error();
    }

    /**
     * Parse the next qualified name or method call declaration.
     * @return new qualified name or method call
     */
    private Value nextQualifiedNameOrCall() {
        // parse the qualified name
        QualifiedName name = nextQualifiedName();

        Value value = new Accessor(name);

        // handle method call
        // println("Hello, World!")
        //        ^ the open parenthesis token after an identifier indicates, that a method call is expected
        if (peek().is(TokenType.OPEN)) {
            // parse the arguments of the method call
            List<Value> arguments = nextArgumentList();
            value = new MethodCall(name, arguments);
        }

        // handle group closing
        // print(foo)
        //          ^ we don't need to handle this closing tag here, just finish qualified name parsing
        if (peek().is(TokenType.CLOSE, TokenType.COMMA, TokenType.STOP, TokenType.END))
            return value;

        // handle single value expression, in which case the local variable is initialized with a single value
        // let myVar = foo;
        //                ^ the (auto-inserted) semicolon indicates, initialized with a single value
        if (peek().is(TokenType.SEMICOLON))
            return value;

        // terminate the literal if an 'else' case of a one-liner 'if' statement is expected
        // let foo = x < 10 ? 1 + 2 : 12 / 6
        //                         ^ terminate the parsing of '1 + 2', as the else case is expected
        if (peek().is(TokenType.COLON))
            return value;

        // handle operation between two expressions
        // let var = foo +
        //               ^ the operator after an identifier indicates, that there are more expressions to be parsed
        //                 the two operands are grouped together by an Operation node
        if (peek().is(TokenType.OPERATOR)) {
            // parse the operator of the operation
            Operator operator = nextOperator();
            // handle field assignation
            if (operator == Operator.ASSIGN)
                return new FieldAssign((Accessor) value, nextValue());
            else if (operator == Operator.QUESTION)
                return nextSelection(value);
            else if (!isComplexOperator(operator.getValue()))
                throw new IllegalStateException("Expected complex operator, but got " + operator);
            return makeOperator(value, operator, nextValue());
        }

        // handle type casting
        // let val = 100 as float
        //                 ^ the 'as' keyword indicates, that the expression has been terminated
        else if (peek().is(TokenType.EXPRESSION, "as")) {
            get();
            return new Casting(value, nextType());
        }

        // handle array indexing
        else if (peek().is(TokenType.START))
            return nextArrayLoadOrStore((Accessor) value);

        System.out.println(ConsoleFormat.RED + "Error (Qualified Name / Call) " + peek());
        return new Error();
    }

    private Value nextArrayLoadOrStore(Accessor accessor) {
        get(TokenType.START);

        Value value = nextValue();
        if (!(value instanceof Literal literal))
            throw new IllegalStateException("Expected literal for array size, but got " + value);

        int index = Integer.parseInt(literal
            .getValue()
            .getValue()
        );

        get(TokenType.STOP);

        if (peek().is(TokenType.SEMICOLON, "auto"))
            get();

        if (peek().is(TokenType.OPERATOR, "=")) {
            get();
            return new ArrayStore(accessor, index, nextValue());
        }

        if (peek().is(TokenType.SEMICOLON))
            get();

        return new ArrayLoad(accessor, index);

    }

    private List<Value> nextArgumentList() {
        // skip the '(' symbol
        get(TokenType.OPEN);
        // handle call arguments
        // foo(123)
        //     ^^^ the tokens in between parenthesis are the arguments
        List<Value> arguments = new ArrayList<>();
        while (!peek().is(TokenType.CLOSE)) {
            // parse the next call argument
            arguments.add(nextValue());
            // check if there are more arguments to be parsed
            if (peek().is(TokenType.COMMA))
                get();
            // argument list declaration ended, exit loop
            else
                break;
        }
        // handle argument list ending
        // baz("John Doe")
        //               ^ the close parenthesis indicates, that the argument list has been ended
        get(TokenType.CLOSE);
        return arguments;
    }

    private Value makeOperator(Value left, Operator operator, Value right) {
        return fixOperationTree(new Operation(left, operator, right));
    }

    private Value fixOperationTree(Value node) {
        // return if the node is not an operation
        if (!(node instanceof Operation operation))
            return node;

        // recursively correct the order of the left and right nodes
        operation.setLeft(fixOperationTree(operation.getLeft()));
        operation.setRight(fixOperationTree(operation.getRight()));

        // check if the current operator has lower precedence than the operator
        // of its right child
        if (operation.getRight() instanceof Operation
                && hasPrecedence(operation.getOperator(), ((Operation) operation.getRight()).getOperator())) {
            // perform a right rotation
            Value temp = operation.getRight();
            operation.setRight(((Operation) temp).getLeft());
            ((Operation) temp).setLeft(operation);
            return temp;
        }

        // check if the current operator has lower or equal precedence than the
        // operator of its left child, and the left child is also an operation
        if (operation.getLeft() instanceof Operation
                && hasPrecedence(operation.getOperator(), ((Operation) operation.getLeft()).getOperator())
                && operation.getOperator().getAssociativity() == 0) {
            // perform a left rotation
            Value temp = operation.getLeft();
            operation.setLeft(((Operation) temp).getRight());
            ((Operation) temp).setRight(operation);
            return temp;
        }

        // the current order is correct, so return the node as it is
        return node;
    }

    /**
     * Check if the first operator has a precedence priority over the second operator.
     * @param first first operator to check
     * @param second second operator to check
     * @return true if the first operator has higher precedence than the second one
     */
    private boolean hasPrecedence(Operator first, Operator second) {
        return first.getPrecedence() > second.getPrecedence()
            || (first.getPrecedence() == second.getPrecedence() && first.getAssociativity() == 0);
    }

    /**
     * Parse the next operator target.
     * @return parsed operator
     */
    private Operator nextOperator() {
        // loop until the token is an operator
        StringBuilder builder = new StringBuilder();
        while (peek().is(TokenType.OPERATOR)) {
            String value = peek().getValue();
            if (shouldOperatorTerminate(builder.toString(), value)) {
                get();
                return Operator.of(builder.toString());
            }
            builder.append(value);
            String operator = builder.toString();
            // check if the current operator has been ended
            if (shouldOperatorTerminate(operator)) {
                get();
                return Operator.of(operator);
            }
            get();
        }
        // handle colons as operators as well
        while (peek().is(TokenType.COLON))
            builder.append(get().getValue());
        return Operator.of(builder.toString());
    }

    private Value nextReferencedQualifiedNameOrCall() {
        get(TokenType.TYPE, "ref");

        // parse the qualified name
        QualifiedName name = nextQualifiedName();

        Value value = new ReferencedAccessor(name);

        // handle method call
        // println("Hello, World!")
        //        ^ the open parenthesis token after an identifier indicates, that a method call is expected
        if (peek().is(TokenType.OPEN))
            throw new IllegalStateException("Referenced method call is not supported yet.");

        // handle group closing
        // print(ref foo)
        //              ^ we don't need to handle this closing tag here, just finish qualified name parsing
        if (peek().is(TokenType.CLOSE, TokenType.COMMA, TokenType.STOP, TokenType.END))
            return value;

        // handle single value expression, in which case the local variable is initialized with a single value
        // let myVar = ref foo;
        //                    ^ the (auto-inserted) semicolon indicates, initialized with a single value
        if (peek().is(TokenType.SEMICOLON))
            return value;

        // terminate the literal if an 'else' case of a one-liner 'if' statement is expected
        // let foo = x < 10 ? 1 + 2 : 12 / 6
        //                         ^ terminate the parsing of '1 + 2', as the else case is expected
        if (peek().is(TokenType.COLON))
            return value;

        // handle operation between two expressions
        // let var = foo +
        //               ^ the operator after an identifier indicates, that there are more expressions to be parsed
        //                 the two operands are grouped together by an Operation node
        if (peek().is(TokenType.OPERATOR))
            throw new IllegalStateException("Referenced operation is not supported yet.");

        System.out.println(ConsoleFormat.RED + "Error (Referenced Qualified Name / Call) " + peek());
        return new Error();
    }

    private Value nextReferencedQualifiedNameOrCall(Referencing referencing) {
        // TODO handle parsed referencing

        // parse the qualified name
        QualifiedName name = nextQualifiedName();

        Value value = new ReferencedAccessor(name);

        // handle method call
        // println("Hello, World!")
        //        ^ the open parenthesis token after an identifier indicates, that a method call is expected
        if (peek().is(TokenType.OPEN))
            throw new IllegalStateException("Referenced method call is not supported yet.");

        // handle group closing
        // print(ref foo)
        //              ^ we don't need to handle this closing tag here, just finish qualified name parsing
        if (peek().is(TokenType.CLOSE, TokenType.COMMA, TokenType.STOP, TokenType.END))
            return value;

        // handle single value expression, in which case the local variable is initialized with a single value
        // let myVar = ref foo;
        //                    ^ the (auto-inserted) semicolon indicates, initialized with a single value
        if (peek().is(TokenType.SEMICOLON))
            return value;

        // terminate the literal if an 'else' case of a one-liner 'if' statement is expected
        // let foo = x < 10 ? 1 + 2 : 12 / 6
        //                         ^ terminate the parsing of '1 + 2', as the else case is expected
        if (peek().is(TokenType.COLON))
            return value;

        // handle operation between two expressions
        // let var = foo +
        //               ^ the operator after an identifier indicates, that there are more expressions to be parsed
        //                 the two operands are grouped together by an Operation node
        if (peek().is(TokenType.OPERATOR))
            throw new IllegalStateException("Referenced operation is not supported yet.");

        System.out.println(ConsoleFormat.RED + "Error (Referenced Qualified Name / Call) " + peek());
        return new Error();
    }


    private Value nextReferenceLocalDeclaration() {
        Referencing referencing = nextReferencing();

        if (!peek().is(TokenType.IDENTIFIER))
            throw new IllegalStateException("Referencing must be followed by an identifier, but got " + peek());

        if (!at(cursor + 1).is(TokenType.OPERATOR, "="))
            return nextReferencedQualifiedNameOrCall(referencing);

        // parse the name of the local variable
        Name name = nextName();

        // check if the name is a tuple destructuring
        if (name.isCompound())
            throw new IllegalStateException("Not supported yet");

        // skip the semicolon after the declaration
        // let variable;
        //             ^ the (auto-inserted) semicolon indicates, that the declaration has been ended
        if (peek().is(TokenType.SEMICOLON))
            get();

        // check if the local variable does not have an initialization declared
        if (!peek().is(TokenType.OPERATOR, "="))
            throw new IllegalStateException("Cannot declare a reference without an initialization");

        // handle the assignation of the local variable
        // let number = 100
        //            ^ the equals sign indicates that the assignation of the local variable has been started
        get(TokenType.OPERATOR, "=");

        // parse the value of the local variable
        // let value = 100 + 50 - 25
        //             ^^^^^^^^^^^^^ the instructions after the equals sign is the value of the local variable
        Value value = nextValue();

        // skip the semicolon after the declaration
        // let variable = 100;
        //                   ^ the (auto-inserted) semicolon indicates, that the assigning variable declaration has been ended
        if (peek().is(TokenType.SEMICOLON))
            get();

        return new ReferenceLocalDeclareAssign(referencing, Type.MUT, ((ScalarName) name).getValue(), value);
    }

    private Value nextMutableLocalDeclaration() {
        // skip the 'let' keyword
        get(TokenType.TYPE, "mut");
        // parse the name of the local variable
        Name name = nextName();
        // check if the name is a tuple destructuring
        if (name.isCompound()) {
            // tuple destructuring requires an initialization, skip the '=' symbol
            // let (a, b) = foo()
            //            ^ the equals sign indicates that the assignation of the local variable has been started
            get(TokenType.OPERATOR, "=");
            // parse the value of the local variable
            // let (code, msg) = requestSomething()
            //                   ^^^^^^^^^^^^^^^^^^ the instructions after the equals sign is the value of the local variable
            Node value = nextExpression();
            // skip the semicolon after the declaration
            // let (a, b, c) = fooBar();
            //                         ^ the (auto-inserted) semicolon indicates, that the assigning variable declaration has been ended
            if (peek().is(TokenType.SEMICOLON))
                get();

            return new LocalDeclareDestructureTuple((CompoundName) name, value);
        }

        // skip the semicolon after the declaration
        // let variable;
        //             ^ the (auto-inserted) semicolon indicates, that the declaration has been ended
        if (peek().is(TokenType.SEMICOLON))
            get();

        // check if the local variable does not have an initialization declared
        if (!peek().is(TokenType.OPERATOR, "="))
            return new LocalDeclare(Type.MUT, ((ScalarName) name).getValue());

        // handle the assignation of the local variable
        // let number = 100
        //            ^ the equals sign indicates that the assignation of the local variable has been started
        get(TokenType.OPERATOR, "=");

        // parse the value of the local variable
        // let value = 100 + 50 - 25
        //             ^^^^^^^^^^^^^ the instructions after the equals sign is the value of the local variable
        Value value = nextValue();

        // skip the semicolon after the declaration
        // let variable = 100;
        //                   ^ the (auto-inserted) semicolon indicates, that the assigning variable declaration has been ended
        if (peek().is(TokenType.SEMICOLON))
            get();

        return new MutableLocalDeclareAssign(Type.MUT, ((ScalarName) name).getValue(), value);
    }

    private Value nextImmutableLocalDeclaration() {
        // skip the 'let' keyword
        get(TokenType.TYPE, "let");
        // parse the name of the local variable
        Name name = nextName();
        // check if the name is a tuple destructuring
        if (name.isCompound()) {
            // tuple destructuring requires an initialization, skip the '=' symbol
            // let (a, b) = foo()
            //            ^ the equals sign indicates that the assignation of the local variable has been started
            get(TokenType.OPERATOR, "=");
            // parse the value of the local variable
            // let (code, msg) = requestSomething()
            //                   ^^^^^^^^^^^^^^^^^^ the instructions after the equals sign is the value of the local variable
            Node value = nextExpression();
            // skip the semicolon after the declaration
            // let (a, b, c) = fooBar();
            //                         ^ the (auto-inserted) semicolon indicates, that the assigning variable declaration has been ended
            if (peek().is(TokenType.SEMICOLON))
                get();

            return new LocalDeclareDestructureTuple((CompoundName) name, value);
        }

        // skip the semicolon after the declaration
        // let variable;
        //             ^ the (auto-inserted) semicolon indicates, that the declaration has been ended
        if (peek().is(TokenType.SEMICOLON))
            get();

        // check if the local variable does not have an initialization declared
        if (!peek().is(TokenType.OPERATOR, "="))
            return new LocalDeclare(Type.LET, ((ScalarName) name).getValue());

        // handle the assignation of the local variable
        // let number = 100
        //            ^ the equals sign indicates that the assignation of the local variable has been started
        get(TokenType.OPERATOR, "=");

        // parse the value of the local variable
        // let value = 100 + 50 - 25
        //             ^^^^^^^^^^^^^ the instructions after the equals sign is the value of the local variable
        Value value = nextValue();

        // skip the semicolon after the declaration
        // let variable = 100;
        //                   ^ the (auto-inserted) semicolon indicates, that the assigning variable declaration has been ended
        if (peek().is(TokenType.SEMICOLON))
            get();

        return new ImmutableLocalDeclareAssign(Type.LET, ((ScalarName) name).getValue(), value);
    }

    /**
     * Indicate, whether the given operator is applicable for a left-right use.
     * @param operator target operator
     * @return true if the operator expects two values
     */
    private boolean isComplexOperator(String operator) {
        // TODO check triple shift operators
        // TODO "?." should be handled by QualifiedName
        return switch (operator) {
            case "+", "+=", "-", "-=", "*", "*=", "/", "/=", "&", "&=", "|", "|=", "&&", "||", "::",
                 "<", "<=", ">", ">=", "==", "!=", ">>", ">>>", "<<", "??", "?", ":", ".", "^", "%"
                    -> true;
            default -> false;
        };
    }

    /**
     * Indicate, whether the given operator is applicable for a single-node use.
     * @param operator target operator
     * @return true if the operator expects one value
     */
    private boolean isSideOperator(String operator) {
        return switch (operator) {
            case "-", "!" -> true;
            default -> false;
        };
    }

    /**
     * Determine if the given operator should be terminated as it is.
     * @param operator target operator
     * @return true if the operator parsing should terminate
     */
    private boolean shouldOperatorTerminate(String operator) {
        return switch (operator) {
            case "&&", "||", "??", "?.", "++", "--", "==", "!=" -> true;
            default -> false;
        };
    }

    private boolean shouldOperatorTerminate(String prev, String next) {
        return switch (prev) {
            case "?" -> !next.equals(".") && !next.equals("?");
            case "=", "!" -> !next.equals("=");
            case "&" -> !next.equals("&");
            default -> false;
        };
    }

    /**
     * Test if the given operator is applicable before a value.
     * @return true if the operator expects a value on its right
     */
    private boolean isLeftOperator(String target) {
        return switch (target) {
            case "!", "++", "--", "+", "-" -> true;
            default -> false;
        };
    }

    /**
     * Test if the given operator is applicable after a value.
     * @return true if the operator expects a value on its left
     */
    private boolean isRightOperator(String target) {
        return switch (target) {
            case "++", "--" -> true;
            default -> false;
        };
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
