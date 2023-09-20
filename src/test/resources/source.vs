package "SelectionTest"

extern int GetStdHandle(int kind)
extern int WriteConsoleA(int handle, ref byte buffer, int length, ref int written, int reserved)

void println(ref byte buffer) {
    let handle = GetStdHandle(-11)
    let written = 0
    WriteConsoleA(handle, buffer, 13, ref written, 0)
    return
}

int main() {
    println("Hello, World!")
    return 100
}
