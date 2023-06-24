import subprocess
from time import time

# Replace "program.exe" with the actual path to your .exe file
exe_path = "output.exe"

# Run the .exe file using subprocess
process = subprocess.Popen(exe_path, stdout=subprocess.PIPE, stderr=subprocess.PIPE)

# Wait for the process to finish and get the exit code
start = time()
exit_code = process.wait()
end = time()

# Print the exit code
print("Exit code:", exit_code)
print("Took", int((end * 1000) - (start * 1000)))
