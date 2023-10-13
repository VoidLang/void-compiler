package "TupleReturnTest"

(int, int) foo() {
    return (10, 20)
}

int main() {
    let x = foo()
    return x.1
}
