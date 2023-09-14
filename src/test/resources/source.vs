package "ClassMethodTest"

class Entity {
    int id = 100

    int tick(mut int x) {
        x = x + 1
        return x
    }
}

int main() {
    let entity = new Entity()

    let val = entity.tick(32)

    return val
}
