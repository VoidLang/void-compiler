clang -S -emit-llvm main.c -o main.ll -v
clang -c -emit-llvm main.c -o main.bc -v
clang -c main.bc -o main.obj
clang main.obj -o app.exe
