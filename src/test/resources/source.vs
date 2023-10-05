void foo(ref int a, ref int b) {
    a = 10
    b = 20
    return
}

int main() {
    mut a = 1
    mut b = 2
    foo(ref a, ref b)
    return a + b
}
