package "ReferenceTest"

int modify(ref int x) {
    let y = deref x
    return y * 2
}

int main() {
    let foo = 100

    return modify(ref foo)
}
