class Car {
    int speed, weight
}

Car createCar() {
    let car    = new Car()
    car.speed  = 10
    car.weight = 20
    return car
}

int main() {
    let car = createCar()
    return car.speed + car.weight
}
