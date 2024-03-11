package util::convert

import os::external::libc
using lib::itoa

ref byte itoa(int value) {
    let buffer = ""
    itoa(value, buffer, 10)
    return buffer
}