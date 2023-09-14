package "ClassMethodTest"

class Entity {
    int id = 100

    void tick() {
    }
}

int main() {
    let entity = new Entity()

    return entity.id
}