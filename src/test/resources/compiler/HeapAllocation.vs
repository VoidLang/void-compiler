int allocInteger() {
    mut ptr = malloc int
    ptr = 100

    let val = deref ptr
    free ptr

    return val
}