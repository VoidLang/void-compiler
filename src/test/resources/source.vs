class Entity {
    int id
    int health
}

int main() {
    let e = new Entity()
    e.id = 10
    e.health = 30

    return e.health + e.id
}
