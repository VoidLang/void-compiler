package "ConditionalTest"

int abs(int x) {
    let f= x < 0
    return x
}

int main() {
    return abs(-12)
}