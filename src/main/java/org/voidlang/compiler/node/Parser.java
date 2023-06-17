package org.voidlang.compiler.node;

import dev.inventex.octa.console.ConsoleFormat;
import dev.inventex.octa.data.primitive.Tuple;
import org.jetbrains.annotations.NotNull;
import org.voidlang.compiler.node.common.Error;
import org.voidlang.compiler.node.common.Finish;
import org.voidlang.compiler.node.control.Return;
import org.voidlang.compiler.node.element.Method;
import org.voidlang.compiler.node.info.PackageImport;
import org.voidlang.compiler.node.info.PackageSet;
import org.voidlang.compiler.node.local.LocalAssign;
import org.voidlang.compiler.node.local.LocalDeclare;
import org.voidlang.compiler.node.local.LocalDeclareAssign;
import org.voidlang.compiler.node.local.LocalDeclareDestructureTuple;
import org.voidlang.compiler.node.operator.Operation;
import org.voidlang.compiler.node.operator.Operator;
import org.voidlang.compiler.node.operator.Value;
import org.voidlang.compiler.node.type.array.Array;
import org.voidlang.compiler.node.type.array.Dimension;
import org.voidlang.compiler.node.type.core.LambdaType;
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
        System.out.println("package \"" + name + '"');
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
        GenericTypeList genericTypes = nextGenericTypes();

        System.out.println(kind + " " + name);
        if (genericTypes.isExplicit()) {
            String debug = genericTypes.getGenerics().stream()
                .map(GenericType::toString)
                .collect(Collectors.joining(", "));
            System.out.print("<" + String.join(", ", debug) + ">");
        }

        // TODO generic type implementation (where T implements MyType)

        System.out.println(ConsoleFormat.RED + "Error (Type)");
        return new Error();
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

        return new NamedTypeGroup(members);
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
                parameters.add(new LambdaParameter(type, variadic, null, false));
                // skip the ',' symbol
                if (peek().is(TokenType.COMMA))
                    get();
                // continue handling parameters, or exit because of the condition
                continue;
            }

            // parse the name of the lambda parameter type
            Name name = nextName();
            // register a named lambda parameter
            parameters.add(new LambdaParameter(type, variadic, name, true));

            // check if there are more parameters to be parsed
            if (peek().is(TokenType.COMMA))
                get();
            // lambda parameter declaration ended, exit the loop
            else
                break;
        }
        // skip the '|' symbol
        get(TokenType.OPERATOR, "|");
        return new LambdaType(returnType, parameters);
    }

    private NamedScalarType nextNamedScalarType(boolean expectName, boolean expectLambda) {
        // parse the type of the named type
        Type type = nextScalarType(expectLambda);
        // check if a name is declared for the type
        String name = "";
        if (expectName && peek().is(TokenType.IDENTIFIER))
            name = get().getValue();
        // handle unnamed scalar type
        return new NamedScalarType(type, name, !name.isEmpty());
    }

    private Type nextType() {
        return nextType(true);
    }

    private Type nextType(boolean expectLambda) {
        // handle type group
        if (peek().is(TokenType.OPEN))
            return nextTypeGroup();
        // handle scalar type
        return nextScalarType(expectLambda);
    }

    private TypeGroup nextTypeGroup() {
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

        return new TypeGroup(members);
    }

    private Type nextScalarType(boolean expectLambda) {
        // check for lambda type declaration without an explicit return type
        if (peek().is(TokenType.OPERATOR, "|"))
            return nextLambdaType(Type.primitive("void"));

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
        ScalarType type = new ScalarType(name, generics, array);

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
            Token token = get(TokenType.IDENTIFIER, TokenType.TYPE /* here again, TYPE might be removed */);
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
            if (peek().is(TokenType.COMMA))
                get();
            // no more parameters expected, exit the loop
            else
                break;
        }

        get(TokenType.CLOSE);

        System.out.println(ConsoleFormat.CYAN + ") " + ConsoleFormat.DARK_GRAY + "{");

        // skip the auto-inserted semicolon before the method body
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

        System.out.println(ConsoleFormat.DARK_GRAY + "}");
        System.out.println();

        // handle method body end
        get(TokenType.END);

        // skip the auto-inserted semicolon
        if (peek().is(TokenType.SEMICOLON))
            get();

        return new Method(type, name, parameters, body);
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
        if (peek().is(TokenType.TYPE, "let"))
            return nextLocalDeclaration();

        // handle variable assignation
        if (peek().is(TokenType.IDENTIFIER) && at(cursor + 1).is(TokenType.OPERATOR, "=")
                && !at(cursor + 2).is(TokenType.OPERATOR, "="))
            return nextLocalAssignation();

        // handle literal constant value
        // let name = "John Doe"
        //            ^^^^^^^^^^ the literal token indicates, that a value is expected
        else if (peek().isLiteral())
            return nextLiteral();

        // handle return statement
        else if (peek().is(TokenType.EXPRESSION, "return"))
            return nextReturnStatement();

        System.out.println(ConsoleFormat.RED + "Error (Expression) " + peek());
        return new Error();
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

        // parse the value to be retured
        Node value = nextExpression();

        // handle the semicolon after the return statement
        if (peek().is(TokenType.SEMICOLON))
            get();

        return new Return(value);
    }

    /**
     * Parse the next local variable value assignation.
     * @return new local assignation
     */
    private Node nextLocalAssignation() {
        // get the name of the local variable
        String name = get().getValue();

        // skip the equals sign
        get(TokenType.OPERATOR, "=");

        // parse the value of the local variable
        Node value = nextExpression();

        // skip the semicolon after the declaration
        if (peek().is(TokenType.SEMICOLON))
            get();

        return new LocalAssign(name, value);
    }

    /**
     * Parse the next literal value declaration.
     * @return new literal
     */
    private Node nextLiteral() {
        // handle literal constant or identifier
        //
        // let name = "John Doe"
        //            ^^^^^^^^^^ the literal token indicates, that a value is expected
        Token token = get(
            TokenType.BOOLEAN, TokenType.CHARACTER, TokenType.STRING,
            TokenType.BYTE, TokenType.SHORT, TokenType.INTEGER,
            TokenType.LONG, TokenType.FLOAT, TokenType.DOUBLE,
            TokenType.HEXADECIMAL, TokenType.BINARY
        );
        Value value = new Value(token);

        // handle single value expression, in which case the local variable is initialized with a single value
        // let myVar = 100;
        //                ^ the (auto-inserted) semicolon indicates, initialized with a single value
        if (peek().is(TokenType.SEMICOLON))
            return value;

        // handle operation between two expressions
        // let var = 100 +
        //               ^ the operator after a literal indicates, that there are more expressions to be parsed
        //                 the two operands are grouped together by an Operation node
        if (peek().is(TokenType.OPERATOR)) {
            // parse the operator of the operation
            Operator operator = nextOperator();
            if (!isComplexOperator(operator.getValue()))
                throw new IllegalStateException("Expected complex operator, but got " + operator);
            return makeOperator(value, operator, nextExpression());
        }

        // TODO handle close, comma, stop, end

        System.out.println(ConsoleFormat.RED + "Error (Literal) " + peek());
        return new Error();
    }

    private Node makeOperator(Node left, Operator operator, Node right) {
        return fixOperationTree(new Operation(left, operator, right));
    }

    private Node fixOperationTree(Node node) {
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
            Node temp = operation.getRight();
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
            Node temp = operation.getLeft();
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
            builder.append(get().getValue());
            String operator = builder.toString();
            // check if the current operator has been ended
            if (shouldOperatorTerminate(operator))
                return Operator.of(operator);
        }
        // handle colons as operators as well
        while (peek().is(TokenType.COLON))
            builder.append(get().getValue());
        return Operator.of(builder.toString());
    }

    private Node nextLocalDeclaration() {
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
        Node value = nextExpression();

        // skip the semicolon after the declaration
        // let variable = 100;
        //                   ^ the (auto-inserted) semicolon indicates, that the assigning variable declaration has been ended
        if (peek().is(TokenType.SEMICOLON))
            get();

        return new LocalDeclareAssign(Type.LET, ((ScalarName) name).getValue(), value);
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
                 "<", "<=", ">", ">=", "==", ">>", ">>>", "<<", "??", "?", ":", ".", "^", "%"
                    -> true;
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
            case "&&", "||" -> true;
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
    private boolean sRightOperator(String target) {
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
