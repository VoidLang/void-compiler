package main

import console
using console::{print,println,panic,exit}

import os::external::libc
using libc::itoa

void checkIndex(int index, int size) {
    if (index < 0 || index >= size) {
        print("Thread paniced with error: index out of bounds: (index: ")
        let buffer = ""
        itoa(index, buffer, 10)
        print(buffer)
        print(", size: ")
        itoa(size, buffer, 10)
        print(buffer)
        println(")")
        exit(101)
    }
    return
}



int main() {
    mut arr = [10, 20, 30]
    let index = 2

    arr[index] = 40

    print("value: ")
    print(123)

    return arr[index]
}

