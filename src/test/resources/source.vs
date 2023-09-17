package "LibraryTest"

extern int GetStdHandle(int kind)

int main() {
    let val = -11
    return GetStdHandle(val)
}