package "ClassAllocationTest"

class Entity {
    int id
}

int main() {
    let entity = new Entity()
    entity.id = 22
    return entity.id
}
