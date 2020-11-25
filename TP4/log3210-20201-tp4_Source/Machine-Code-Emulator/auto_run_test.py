import sys,os

FILE_FIB = lambda x: "fib_separated/fibb_part" + str(x) +".asm"
FILE_RES = lambda x: "../test-suite/PrintMachineCodeTest/result/block" + str(x[0]) + "_" + str(x[1]) + ".ci"
FILE_OUT = "examples/fibb.asm"
# with open("Machine-Code-Emulator/fib_separated", "")

# print(os.listdir("Machine-Code-Emulator/fib_separated"))
# print(os.listdir("../test-suite/PrintMachineCodeTest/result/"))


def list_file(s):
    s = str(s)
    l = [FILE_FIB(0),
         FILE_RES([1,s]),
         FILE_FIB(1),
         FILE_RES([2,s]),
         FILE_FIB(2)]
    return l

def out_file(s):
    with open(FILE_OUT, "w") as f_out:
        for filename in list_file(s):
            with open(filename, "r") as f:
                text = f.read()
                f_out.write(text)
                

# Marche sans contrainte ? (10 points)
s = "Test without register limitation"
print("-" * len(s) + '\n' + s +'\n' + "-" * len(s))
out_file("full")
os.system("python run_tests.py 256")

# Marche avec Contrainte 3 registres (3 points)
s = "Test with 3 registers limitation"
print("-" * len(s) + '\n' + s +'\n' + "-" * len(s))
out_file(3)
os.system("python run_tests.py 3")

# Marche avec Contrainte 5 registres (3 points)
s = "Test with 5 registers limitation"
print("-" * len(s) + '\n' + s +'\n' + "-" * len(s))
out_file(5)
os.system("python run_tests.py 5")


# Marche avec Contrainte 11 registres (3 points)
s = "Test with 5 registers limitation"
print("-" * len(s) + '\n' + s +'\n' + "-" * len(s))
out_file("full")
os.system("python run_tests.py 11")



