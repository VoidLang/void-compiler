; ModuleID = 'test_module'
source_filename = "test_module"
target datalayout = "e-m:w-p270:32:32-p271:32:32-p272:64:64-i64:64-f80:128-n8:16:32:64-S128"

@text = global [13 x i8] c"Hello, World!"

declare i32 @GetStdHandle(i32) #0

declare i32 @WriteConsoleA(i32, ptr, i32, ptr, i32) #0

define void @println(ptr %0) #0 {
entry:
  %"let (ptr) handle" = alloca i32, align 4
  %1 = call i32 @GetStdHandle(i32 -11)
  store i32 %1, ptr %"let (ptr) handle", align 4
  %"let (ptr) written" = alloca i32, align 4
  store i32 0, ptr %"let (ptr) written", align 4
  %handle = load i32, ptr %"let (ptr) handle", align 4
  %2 = call i32 @WriteConsoleA(i32 %handle, ptr %0, i32 13, ptr %"let (ptr) written", i32 0)
  ret void
}

define i32 @main() #0 {
entry:
  call void @println(ptr @text)
  ret i32 100
}

attributes #0 = { "frame-pointer"="none" }
