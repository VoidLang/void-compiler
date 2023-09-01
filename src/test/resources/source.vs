class List {
    Element first
}

class Element {
    int value = 200
    Element next
}

List createList() {
    let list = new List()
    return list
}

Element addElement(List list, int value) {
    let element = new Element()
    return element
}

int main() {
    let list = createList()
    return 202
}
