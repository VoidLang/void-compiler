package "PointerTest"

void bar(ref int x) {
    x = 200
}

int main() {
    let foo = 100
    bar(ref foo)
    return foo
}
