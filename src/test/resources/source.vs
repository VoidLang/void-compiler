package "HeapAllocationTest"

int main() {
    mut val = malloc int
    val = 100
    let x = deref val
    free val
    return x
}
