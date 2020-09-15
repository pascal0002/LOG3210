package analyzer;

import analyzer.ast.*;
import analyzer.visitors.SemantiqueVisitor;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;

/**
 * Created: 17-08-02
 * Last Changed: 17-08-02
 * Author: Nicolas Cloutier
 *
 * Description: This class contains one of the two entry points of the program.
 * (The other one is TestRunner.main()). But Main.Run() is used by both entries.
 */

public class Main {

    // It is the entry point of the programs
    // The arguments are:
    // arg[0] (Required): The path of the input file to parse
    // arg[1] (Optional): The path of the output file, will print to System.out if missing.
    public static void main(String[] args) {
        if(args.length <= 0) {
            System.err.println("args[0] is missing! (The file to read)");
            return;
        }

        InputStream file;
        try {
            file = new java.io.FileInputStream(args[0]);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        // For this test we are using the print visitor, but you can change this
        // to whatever you want.
        PrintWriter pw = new PrintWriter(System.out);
        try {
            Run(new SemantiqueVisitor(pw), file, pw);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    // This is the main function of the program, it is used by main and it is used
    // by the test cases.
    public static void Run(ParserVisitor visitor, InputStream input, PrintWriter output) throws ParseException {

        // This line ask the parser built from the jjt file to read & parse the input file
        ASTProgram root = Parser.ParseTree(input);

        // After this we pass the visitor to the root of the parsed tree
        root.jjtAccept(visitor, null);

        // If the visitor has printed in the output, make sure everything is flushed
        output.flush();
    }
}
