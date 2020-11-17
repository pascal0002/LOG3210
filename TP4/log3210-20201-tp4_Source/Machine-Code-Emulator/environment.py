import numpy

from exceptions import SimulationError


# The memory box is a little container for the values so we can pass them by reference and change the values easily
# even if it's from a register or from memory
class MemoryBox:
    value = 0

    def __init__(self, value=0):
        self.value = value

    def __str__(self):
        return str(self.value)


class Environment:
    # Instruction labels
    __labels = {}

    # Registers data
    __nb_registers = 0
    __registers = []

    # Memory data
    __memory_size = 0
    __memory = []
    __variables = {}

    __bloc_size__ = 32

    # Init variables
    def __init__(self, nb_registers, memory_size, labels):
        self.__labels = labels

        self.__nb_registers = nb_registers
        self.__registers = [MemoryBox() for _ in range(nb_registers)]

        # Special contraint to be able to print memory
        if memory_size % self.__bloc_size__ != 0:
            raise ValueError("Memory size must be a multiple of " + str(self.__bloc_size__))
        self.__memory_size = memory_size
        self.__memory = [MemoryBox() for _ in range(self.__memory_size)]

    # Protect the get of a variable to ensure a correct exception if needed
    def fetch_variable(self, variable, can_create_variable=False):
        # Try to get the memory address

        if not (variable[0].isalpha() and variable.isalnum()):
            raise SimulationError("Variable «" + variable + "» is not a valid name.")

        # Manage the variable creation if needed
        if variable not in self.__variables:
            if not can_create_variable:
                raise SimulationError("The variable «" + variable + "» cannot be found!")
            elif len(self.__variables) == self.__memory_size:
                raise SimulationError("The maximum number of variables has been reached!")
            self.__variables[variable] = len(self.__variables)

        return self.__variables[variable]

    # Protect the register from OOB
    def fetch_register(self, index):
        if index < 0 or index >= self.__nb_registers:
            raise SimulationError("«R" + str(index) + "» is out of bound")
        return self.__registers[index]

    # Protect the memory from OOB
    def fetch_memory(self, address):
        if address < 0 or address >= self.__memory_size:
            raise SimulationError("«R" + str(address) + "» is out of bound")
        return self.__memory[address]

    def fetch_label_mapping(self, label):
        if label not in self.__labels:
            raise SimulationError("«" + label + "» has not been found in the program")
        return self.__labels[label]

    # Pretty Print
    def print(self):
        registers = [r.value for r in self.__registers]
        memory = [m.value for m in self.__memory]
        print("\nState of simulation: ")
        print("Registers: " + str(registers))

        print("\nMemory:")
        row_size = self.__bloc_size__
        column_size = int(self.__memory_size / row_size)

        matrix = numpy.reshape(memory, (column_size, row_size))
        print(matrix)

    def clear(self):
        for reg in self.__registers:
            reg.value = 0
