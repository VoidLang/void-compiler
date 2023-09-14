package "ClassMethodTest"

class Entity {
    int id = 100

    void tick() {
        this.id = 200
    }
}

int main() {
    let entity = new Entity()

    entity.tick()

    return entity.id
}
