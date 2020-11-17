import time

from arpeggio import PTNodeVisitor, visit_parse_tree

from environment import Environment, MemoryBox
from exceptions import SimulationError


# Visitor for the preprocessing phase
# What we do:
# 1. Fetch all the label tags we can jump to
# 2. Memorize the label target on jump node directly
# 3. Register the memory usage (read or write)

class PreprocessVisitor(PTNodeVisitor):
    labels = {}

    def visit__default__(self, node, children):
        return None

    def visit_program(self, node, children):
        node.labels = self.labels

    def visit_load_operation(self, node, children):
        node[1].memory_mode = "write"

    def visit_store_operation(self, node, children):
        node[1].memory_mode = "write"

    def visit_binary_computation_operation(self, node, children):
        node[1].memory_mode = "write"

    def visit_unary_computation_operation(self, node, children):
        node[1].memory_mode = "write"  # In reality it's write AND read, but whatever

    def visit_conditional_jump_operation(self, node, children):
        node.label_target = node[-1].value

    def visit_unconditional_jump_operation(self, node, children):
        node.label_target = node[-1].value

    def visit_print_operation(self, node, children):
        node[1].memory_mode = "read"

    def visit_input_operation(self, node, children):
        node[1].memory_mode = "write"

    def visit_memory(self, node, children):
        # Default mode is read
        node.memory_mode = "read"

    def visit_constant(self, node, children):
        # Default mode is read
        node.memory_mode = "read"

    def visit_register(self, node, children):
        # Default mode is read
        node.memory_mode = "read"

    def visit_label_marker(self, node, children):
        # Memorize the labels
        self.labels[node[0].value] = node.instruction


# It's the main visitor that is simulating all the instructions
# Environment is a class that is storing all the runtime data and the labels (does not store the instruction)
# The root is not the program, but an instruction (We manage the visit of the instructions at a higher level).
# The children are the return values from the children of a node. Their contents change depending of the type.
# By example, an operation may return the next instruction, the register will return the value in a container
class SimulatorVisitor(PTNodeVisitor):
    environment = None

    def __init__(self, environment):
        self.environment = environment
        super().__init__()

    def visit__default__(self, node, children):
        return None

    def visit_instruction(self, node, children):
        # The child of an instruction is a label and an operation
        # The only result from childs are next operations
        next_instruction = node.instruction + 1
        if len(children) == 1:
            next_instruction = children[-1]
        elif len(children) > 1:
            raise SimulationError("Unexpected number of children")

        return next_instruction

    def visit_load_operation(self, node, children):
        children[0].value = children[1].value

    def visit_store_operation(self, node, children):
        children[0].value = children[1].value

    def visit_binary_computation_operation(self, node, children):
        dest = children[0]
        src1 = children[1].value
        src2 = children[2].value

        if node[0].value == "ADD":
            dest.value = src1 + src2
        elif node[0].value == "SUB":
            dest.value = src1 - src2
        elif node[0].value == "MUL":
            dest.value = src1 * src2
        elif node[0].value == "DIV":
            dest.value = int(src1 / src2)
        elif node[0].value == "MOD":
            dest.value = src1 % src2

    def visit_unary_computation_operation(self, node, children):
        if node[0].value == "INC":
            children[0].value += 1
        elif node[0].value == "DEC":
            children[0].value -= 1

    def visit_conditional_jump_operation(self, node, children):
        jump = False
        op = node[0].value
        val = children[0].value

        if op == "BGTZ":
            jump = val > 0
        elif op == "BGETZ":
            jump = val >= 0
        elif op == "BLTZ":
            jump = val < 0
        elif op == "BLETZ":
            jump = val <= 0
        elif op == "BETZ":
            jump = val == 0
        elif op == "BNETZ":
            jump = val != 0

        if jump:
            return self.environment.fetch_label_mapping(node.label_target)

    def visit_unconditional_jump_operation(self, node, children):
        # Fetch the next operation and return it
        return self.environment.fetch_label_mapping(node.label_target)

    def visit_print_operation(self, node, children):
        print(str(children[0]))

    def visit_input_operation(self, node, children):
        try:
            value = int(input())
            children[0].value = value
        except:
            print("Invalid input!")
            exit(-1)

    def visit_clear_operation(self, node, children):
        self.environment.clear()

    def visit_memory(self, node, children):
        # "*"? (identifier / number) ("(" register ")")?

        # Children 0 is a string or an int
        if type(children[0]) is str:
            address = self.environment.fetch_variable(children[0], node.memory_mode == "write")
        else:
            address = children[0]

        # We are sure that there is (something)
        if len(children) > 1:
            address += children[-1].value

        # Dereference the address
        if node[0].value == "*":
            address = self.environment.memory[address].value

        return self.environment.fetch_memory(address)

    def visit_constant(self, node, children):
        return MemoryBox(children[-1])

    def visit_register(self, node, children):
        return self.environment.fetch_register(children[-1])

    def visit_identifier(self, node, children):
        return str(node.value)

    def visit_number(self, node, children):
        return int(node.value)

    def visit_string(self, node, children):
        return str(node.value)[2:-2]


# Simulator is the main class with the duty to run a simulation
class Simulator:
    # The root of the tree
    root = None

    def __init__(self, tree, reg):
        self.root = tree
        self.MAX_REG = reg
        # Preprocess the tree
        visit_parse_tree(self.root, PreprocessVisitor())

    def simulate(self, max_time):
        # TODO: here you can change the number of register you want to use
        # Prepare the environment for the simulation
        # 3 registers and 256 of memory
        environment = Environment(self.MAX_REG, 256, self.root.labels) 

        start_time = time.time()

        current_instruction = 0
        current_line = self.root[current_instruction].line

        try:
            # Execute the simulation
            while True:
                # While the time has not been exceeded
                elapsed_time = time.time() - start_time
                if elapsed_time > max_time:
                    raise SimulationError("Maximum time allowed for the simulation exceeded!")

                # Check if the simulation is over
                if current_instruction >= len(self.root) - 1:
                    break

                # Do the instruction logic
                current_instruction = visit_parse_tree(self.root[current_instruction], SimulatorVisitor(environment))
                current_line = self.root[current_instruction].line

        # Add the line who crashed so it's easier to debug
        except SimulationError as error:
            message, = error.args
            raise SimulationError(message + " occurred at line " + str(current_line))

        # Print the finale results
        environment.print()
