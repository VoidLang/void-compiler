package "PassByReferenceTest"

class PassByReference {
    int value = 1337
}

void modifyReference(PassByReference reference) {
    reference.value = reference.value - 337
    return
}

int main() {
    let reference = new PassByReference()
    modifyReference(reference)
    return reference.value
}
