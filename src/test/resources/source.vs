package "SelectionTest"

extern int GetStdHandle(int kind)

extern int WriteConsoleA(int handle, ref byte buffer, int length, ref int written, int reserved)

extern int strlen(ref byte buffer)

void println(ref byte buffer) {
    let handle = GetStdHandle(-11)
    let written = 0
    let len = strlen(buffer)
    WriteConsoleA(handle, buffer, len, ref written, 0)
    return
}

int fib(int n) {
    if (n == 0 || n == 1)
        return n
    return fib(n - 1) + fib(n - 2)
}

int main() {
    fib(20)
    println("Hello, World You Fool!")
    return 100
}
