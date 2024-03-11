package main

import console
using console::print

int main() {
    mut arr = [10, 20, 30]
    let index = 45

    arr[index] = 40
    arr[index] = 20

    print("value is: ")
    print(arr[index])

    return 1
}
