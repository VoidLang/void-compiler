package main

import console
using console::print

import os::win::kernel32
using kernel32::strlen

class String {
    ref byte buffer
    int length

    int length() {
        return this.length
    }
}

String newString(ref byte buffer) {
    let str = new String()

    str.buffer = buffer
    str.length = strlen(buffer)

    return str
}

int main() {
    let str = newString("abc")

    print(str.length())

    return 0
}
