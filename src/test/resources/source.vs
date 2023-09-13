package "MutablePrimitiveParamTest"

int foo(mut int a) {
    a = 10
    return a
}

int main() {
    return foo(100)
}