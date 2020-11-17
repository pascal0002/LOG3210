import os
import sys

from simulator import Simulator
from exceptions import SimulationError

MAX_REG = 256
if len(sys.argv) == 2:
    MAX_REG = int(sys.argv[1])

# Load the parser with the correct grammar
from arpeggio.cleanpeg import ParserPEG, NoMatch, PTNodeVisitor, visit_parse_tree

grammar = open(os.path.join(os.path.dirname(__file__), 'grammar.peg'), 'r').read()
parser = ParserPEG(grammar, "program", "comment", debug=False)


# A little visitor to insert instructions and lines informations in the tree
class SetInstructionVisitor(PTNodeVisitor):
    instruction = -1
    line = -1

    def __init__(self, instruction, line):
        self.instruction = instruction
        self.line = line
        super().__init__(self)

    def visit__default__(self, node, children):
        node.instruction = self.instruction
        node.line = self.line


def run_test(filename):
    with open(filename, 'r') as program:
        try:
            # Parse the tree
            raw_text = program.read()
            tree = parser.parse(raw_text)

            # Insert attributes in the tree
            for index, inst in enumerate(tree):
                line, col = parser.pos_to_linecol(inst.position)
                visit_parse_tree(inst, SetInstructionVisitor(index, line))

            # Prepare the simulation (Preprocess what we can)
            simulator = Simulator(tree,MAX_REG)

            # Execute the simulation
            simulator.simulate(5)  # Maximum of 5 seconds for the simulation
            print("Test of «" + filename + "» Succeeded")

        except NoMatch as error:
            print("Test of «" + filename + "» failed during the parsing: " + str(error))
        except SimulationError as error:
            print("Test of «" + filename + "» Failed during the execution: " + repr(error))


# Fetch all examples and test them
for root, dirs, files in os.walk("examples/"):
    for file in files:
        if file.endswith(".asm"):
            run_test(os.path.join(root, file))
