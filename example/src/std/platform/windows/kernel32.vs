package platform::windows::kernel32

extern int GetStdHandle(int kind)

extern int WriteConsoleA(int handle, ref byte buffer, int length, ref int written, int reserved)

extern int strlen(ref byte buffer)
