int foo(ref int a, ref int b) {
    let x = deref a
    let y = deref b
    return x + y
}

int main() {
    let a = 2
    let b = 3
    return foo(ref a, ref b)
}
