# Machine-Code-Emulator

## Description

This project is a little simulator of a basic machine code language from the book « Compilers: Principles, Techniques & Tools - 2nd edition» in the section 8.2.1. 

## Project information

### Examples
The folder examples contains the programs that will be simulated. Just add any file with an «.asm» extension to test it.

### auto_run_tests.py
This is the main script of the project. Just execute it without arguments to execute the simulation on multiple register limitation

### run_tests.py
This is the main script of the project. Just execute it with one argument (your register limitation) to execute the simulation

### grammar.peg
This is the file used to parse your file and to build the syntax tree

### simulator.py
Contains the heavy logic of the simulation

### environment.py
Hold the runtime data for the simulation

## Depedencies
* Python 3.6
* Numpy (install with «pip install numpy»)
* Arpeggio (install with «pip install arpeggio»)
