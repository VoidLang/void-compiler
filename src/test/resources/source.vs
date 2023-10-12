package "StructTest"

struct Foo {
    int x
}

int main() {
    let a = new Foo()
    a.x = 32
    return a.x
}
