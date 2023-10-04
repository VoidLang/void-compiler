class Entity {
    int id
    int health = 20
}

int getEntityHealth(Entity entity) {
    return entity.health + entity.id
}

int main() {
    let entity = new Entity()
    entity.id = 1
    return getEntityHealth(entity)
}
