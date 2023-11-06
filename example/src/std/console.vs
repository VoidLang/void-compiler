package console

import platform::windows::kernel32
using platform::windows::kernel32::{GetStdHandle,WriteConsoleA,strlen}

/**
 * Print a buffer of a fixed size to the standard output stream.
 */
void printSizedBuffer(ref byte buffer, int length) {
    // get the current stdout handle
    let handle = GetStdHandle(-11)
    let written = 0
    // write the buffer to the stdout
    WriteConsoleA(handle, buffer, length, ref written, 0)
    return
}

/** 
 * Print a value ot the standard output stream.
 */
void print(ref byte buffer) {
    let len = strlen(buffer)
    printSizedBuffer(buffer, len)
    return
}

/**
 * Print a value to the standard output stream and terminate the line.
 */
void println(ref byte buffer) {
    print(buffer)
    printSizedBuffer("\r\n", 2)
    return
}

/**
 * Print a new line to the standard output stream.
 */
void println() {
    printSizedBuffer("\r\n", 2)
    return
}
