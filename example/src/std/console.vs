package console

import platform::windows::kernel32
using platform::windows::kernel32::{GetStdHandle,WriteConsoleA,strlen}

/**
 * Print a buffer of a fixed size to the standard output stream.
 */
void printSizedBuffer(ref byte buffer, int length) {
    // get the current standard output handle
    let handle = GetStdHandle(-11)
    let written = 0
    // write the buffer to the standard output
    WriteConsoleA(handle, buffer, length, ref written, 0)
    return
}

/** 
 * Print a value ot the standard output stream.
 */
void print(ref byte buffer) {
    // calculate the length of the buffer
    let len = strlen(buffer)
    // write the buffer to the standard output with a fixed size
    printSizedBuffer(buffer, len)
    return
}

/**
 * Print a value to the standard output stream and terminate the line.
 */
void println(ref byte buffer) {
    // calculate the length of the buffer and write it to the console
    print(buffer)
    // write a leading new line
    printSizedBuffer("\r\n", 2)
    return
}

/**
 * Print a new line to the standard output stream.
 */
void println() {
    // write a single new line indicator to the console
    printSizedBuffer("\r\n", 2)
    return
}
