import subprocess

executable_path = 'app.exe'

try:
    # Run the executable and capture its return code
    return_code = subprocess.call(executable_path)

    print("Status code", return_code)

except FileNotFoundError:
    print(f"The executable {executable_path} was not found.")
except Exception as e:
    print(f"An error occurred: {str(e)}")
