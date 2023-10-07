package "ReferenceDeclarationTest"

class Entity {
    int id
}

int main() {
    ref entity = new Entity()
    entity.id = 200
    return entity.id
}
