package "PassByReferenceTest"

class PassByReference {
    int value = 1337
}

void modifyReference(PassByReference reference) {
    reference.value = 42
    return
}

int main() {
    let reference = new PassByReference()
    modifyReference(reference)
    return reference.value
}