void foo(ref int a, ref int b) {
    a = 10
    b = 20
    return
}

int passAsReference() {
    mut a = 1
    mut b = 2
    foo(ref a, ref b)
    return a + b
}

int passPointers() {
    ref a = 1
    ref b = 2
    foo(a, b)
    let x = deref a
    let y = deref b
    return x + y
}
