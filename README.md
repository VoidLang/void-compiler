# VoidCompiler

A temporary compiler written in Java that compiles Void source code to machine code using LLVM.

## Installing
You will need to clone the following 3 maven projects:
- This project
- https://github.com/inventex-development/octacore
- https://github.com/VoidHttp/OptionParser

Make sure to build locally these three projects. I will eventually upload them to a public maven repository, so you wouldn't have to download them manually.

## Documentation
The compiler source code can be found in `src/main/java/org/voidlang/compiler`. 
This folder consists of the following parts:
- `cli`: The command line interface for the compiler. This can generate and compile Void projects.
- `token`: The tokenization process of the compiler. This converts the source code into a list of tokens.
- `node`: The AST (Abstract Syntax Tree) of the compiler. This converts the list of tokens into a tree structure.
- `config`: The configuration of the compiler. These are parsed by the project's `void.toml` file.
- `util`: Some utility classes used by the compiler.
- `builder`: The Void application handler. This is the root of the compiling process.

## Want to try yourself?
Edit the `source.vs` file in `src/test/resources`. Try out the examples provided below.
To compile the source code, run the `ParserTest` in `src/test/java/parser`. 
The compiled code can be located in the `debug` folder.
The LLVM bitcode will be generated in `dump.ll`. Finally, run `run.exe` to execute the program.

## Examples

`Create a loop from 0 to 9`
```cs
int main() {
    mut i = 0
    while (i < 10) {
        i = i + 1
    }
    return i
}
```

`Create a fibonacci sequence`
```cs
int fib(int n) {
    if (n == 0 || n == 1)
        return n
    return fib(n - 1) + fib(n - 2)
}

int main() {
    return fib(10)
}
```

`An example to write "Hello World" to the console.`
```cs
extern int GetStdHandle(int kind)

extern int WriteConsoleA(int handle, ref byte buffer, int length, ref int written, int reserved)

extern int strlen(ref byte buffer)

void printSizedBuffer(ref byte buffer, int length) {
    let handle = GetStdHandle(-11)
    let written = 0
    WriteConsoleA(handle, buffer, length, ref written, 0)
    return
}

void print(ref byte buffer) {
    let len = strlen(buffer)
    printSizedBuffer(buffer, len)
    return
}

void println(ref byte buffer) {
    print(buffer)
    printSizedBuffer("\r\n", 2)
    return
}

void println() {
    printSizedBuffer("\r\n", 2)
    return
}

int main() {
    println("Hello World")
    return 0
}
```
