; ModuleID = 'test_module'
source_filename = "test_module"
target datalayout = "e-m:w-p270:32:32-p271:32:32-p272:64:64-i64:64-f80:128-n8:16:32:64-S128"

@text.0 = global [3 x i8] c"\0D\0A\00"
@text.1 = global [3 x i8] c"\0D\0A\00"
<<<<<<< Updated upstream
=======
@text.2 = global [6 x i8] c"start\00"
@text.3 = global [4 x i8] c"end\00"
>>>>>>> Stashed changes

declare i32 @GetStdHandle(i32) #0

declare i32 @WriteConsoleA(i32, ptr, i32, ptr, i32) #0

declare i32 @strlen(ptr) #0

define void @printSizedBuffer(ptr %0, i32 %1) #0 {
entry:
  %"let (ptr) handle" = alloca i32, align 4
  %2 = call i32 @GetStdHandle(i32 -11)
  store i32 %2, ptr %"let (ptr) handle", align 4
  %"let (ptr) written" = alloca i32, align 4
  store i32 0, ptr %"let (ptr) written", align 4
  %handle = load i32, ptr %"let (ptr) handle", align 4
  %3 = call i32 @WriteConsoleA(i32 %handle, ptr %0, i32 %1, ptr %"let (ptr) written", i32 0)
  ret void
}

define void @print(ptr %0) #0 {
entry:
  %"let (ptr) len" = alloca i32, align 4
  %1 = call i32 @strlen(ptr %0)
  store i32 %1, ptr %"let (ptr) len", align 4
  %len = load i32, ptr %"let (ptr) len", align 4
  call void @printSizedBuffer(ptr %0, i32 %len)
  ret void
}

define void @println(ptr %0) #0 {
entry:
  call void @print(ptr %0)
  call void @printSizedBuffer(ptr @text.0, i32 2)
  ret void
}

define void @println.1() #0 {
entry:
  call void @printSizedBuffer(ptr @text.1, i32 2)
  ret void
}

define i32 @main() #0 {
entry:
<<<<<<< Updated upstream
  %"declare assign (ptr) x" = alloca i32, align 4
  store i32 0, ptr %"declare assign (ptr) x", align 4
  br label %while

while:                                            ; preds = %do, %entry
  %x = load i32, ptr %"declare assign (ptr) x", align 4
  %lt = icmp slt i32 %x, 10
  br i1 %lt, label %do, label %merge

do:                                               ; preds = %while
  %x1 = load i32, ptr %"declare assign (ptr) x", align 4
  %add = add i32 %x1, 1
  store i32 %add, ptr %"declare assign (ptr) x", align 4
  br label %while

merge:                                            ; preds = %while
  %x2 = load i32, ptr %"declare assign (ptr) x", align 4
  ret i32 %x2
=======
  call void @println(ptr @text.2)
  %"let (ptr) i" = alloca i32, align 4
  store i32 0, ptr %"let (ptr) i", align 4
  br label %while

while:                                            ; preds = %do, %entry
  %i = load i32, ptr %"let (ptr) i", align 4
  %lt = icmp slt i32 %i, 10
  br i1 %lt, label %do, label %merge

do:                                               ; preds = %while
  %i1 = load i32, ptr %"let (ptr) i", align 4
  %add = add i32 %i1, 1
  store i32 %add, ptr %"let (ptr) i", align 4
  br label %while

merge:                                            ; preds = %while
  call void @println(ptr @text.3)
  %i2 = load i32, ptr %"let (ptr) i", align 4
  ret i32 %i2
>>>>>>> Stashed changes
}

attributes #0 = { "frame-pointer"="none" }
