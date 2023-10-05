int sum(ref int a, ref int b) {
    let x = deref a
    let y = deref b
    return x + y
}

int main() {
    ref a = 5
    ref b = 2
    return sum(a, b)
}
