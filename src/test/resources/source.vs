class List {
    Element first
}

class Element {
    int value = 99
    Element next
}

int main() {
    let list = new List()
    
    let element = new Element()
    element.value = 10

    list.first = element

    return element.value
}
