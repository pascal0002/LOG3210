package analyzer.visitors;

import analyzer.ast.*;

import java.io.PrintWriter;
import java.util.*;

public class PrintMachineCodeVisitor implements ParserVisitor {

    private PrintWriter m_writer = null;

    private Integer REG = 256; // default register limitation
    private ArrayList<String> RETURNED = new ArrayList<String>(); // returned variables from the return statement
    private ArrayList<MachLine> CODE   = new ArrayList<MachLine>(); // representation of the Machine Code in Machine lines (MachLine)

    private ArrayList<String> MODIFIED = new ArrayList<String>(); // could be use to keep which variable/pointer are modified while going through the intermediate code
    private ArrayList<String> REGISTERS = new ArrayList<String>();; // map to get the registers

    private HashMap<String,String> OPMap; // map to get the operation name from it's value
    public PrintMachineCodeVisitor(PrintWriter writer) {
        m_writer = writer;

        OPMap = new HashMap<>();
        OPMap.put("+", "ADD");
        OPMap.put("-", "MIN");
        OPMap.put("*", "MUL");
        OPMap.put("/", "DIV");
    }

    @Override
    public Object visit(SimpleNode node, Object data) {
        return null;
    }

    @Override
    public Object visit(ASTProgram node, Object data) {
        // Visiter les enfants
        node.childrenAccept(this, null);

        compute_LifeVar();   // Life variables computation from the backward visit of CODE
        compute_NextUse();   // Next-Use computation from the backward visit of CODE
        print_machineCode(); // generate the machine code from the forward visit of CODE


        return null;
    }

    @Override
    public Object visit(ASTNumberRegister node, Object data) {
        REG = ((ASTIntValue) node.jjtGetChild(0)).getValue(); // get the limitation of register
        return null;
    }

    @Override
    public Object visit(ASTReturnStmt node, Object data) {
        for(int i = 0; i < node.jjtGetNumChildren(); i++) {
            RETURNED.add(((ASTIdentifier) node.jjtGetChild(i)).getValue()); // returned values
        }

        return null;
    }

    @Override
    public Object visit(ASTBlock node, Object data) {
        node.childrenAccept(this, null);
        return null;
    }

    @Override
    public Object visit(ASTStmt node, Object data) {
        node.childrenAccept(this, null);
        return null;
    }

    @Override
    public Object visit(ASTAssignStmt node, Object data) {
        // On ne visite pas les enfants puisque l'on va manuellement chercher leurs valeurs
        // On n'a rien a transférer aux enfants
        String assign = (String) node.jjtGetChild(0).jjtAccept(this, null);
        String left   = (String) node.jjtGetChild(1).jjtAccept(this, null);
        String right  = (String) node.jjtGetChild(2).jjtAccept(this, null);
        String op     = node.getOp();

        // TODO: Modify CODE to add the needed MachLine.
        //       here the type of Assignment is "assigned = left op right"
        CODE.add(new MachLine(op, assign, left, right));

        return null;
    }

    @Override
    public Object visit(ASTAssignUnaryStmt node, Object data) {
        // On ne visite pas les enfants puisque l'on va manuellement chercher leurs valeurs
        // On n'a rien a transférer aux enfants
        String assign = (String) node.jjtGetChild(0).jjtAccept(this, null);
        String right  = (String) node.jjtGetChild(1).jjtAccept(this, null);

        // TODO: Modify CODE to add the needed MachLine.
        //       here the type of Assignment is "assigned = - right"
        //       suppose the left part to be the constant #O
        CODE.add(new MachLine("-", assign, "#", right));

        return null;
    }

    @Override
    public Object visit(ASTAssignDirectStmt node, Object data) {
        // On ne visite pas les enfants puisque l'on va manuellement chercher leurs valeurs
        // On n'a rien a transférer aux enfants
        String assign = (String) node.jjtGetChild(0).jjtAccept(this, null);
        String right  = (String) node.jjtGetChild(1).jjtAccept(this, null);

        // TODO: Modify CODE to add the needed MachLine.
        //       here the type of Assignment is "assigned = right"
        //       suppose the left part to be the constant #O

        CODE.add(new MachLine("+", assign, "#0", right));

        return null;
    }

    @Override
    public Object visit(ASTExpr node, Object data) {
        return node.jjtGetChild(0).jjtAccept(this, null);
    }

    @Override
    public Object visit(ASTIntValue node, Object data) {
        return "#"+String.valueOf(node.getValue());
    }

    @Override
    public Object visit(ASTIdentifier node, Object data) {
        return node.getValue();
    }


    private class NextUse {
        // NextUse class implementation: you can use it or redo it you're way
        public HashMap<String, ArrayList<Integer>> nextuse = new HashMap<String, ArrayList<Integer>>();

        // Constructor without parameter
        public NextUse() {}

        // Constructor with parameter
        public NextUse(HashMap<String, ArrayList<Integer>> nextuse) {
            this.nextuse = nextuse;
        }

        // Get function: gets the arraylist of nextuses line numbers for the string s
        public ArrayList<Integer> get(String s) {
            return nextuse.get(s);
        }

        // Add function: add the value i at the next use array for the variable s
        public void add(String s, int i) {
            if (!nextuse.containsKey(s)) {
                nextuse.put(s, new ArrayList<Integer>());
            }
            nextuse.get(s).add(i);
        }

        // To string function
        public String toString() {
            String buff = "";
            boolean first = true;
            for (String k : set_ordered(nextuse.keySet())) {
                if (! first) {
                    buff +=", ";
                }
                Collections.sort(nextuse.get(k));
                buff += k + ":" + nextuse.get(k);
                first = false;
            }
            return buff;
        }

        // Clone function
        public Object clone() {
            return new NextUse((HashMap<String, ArrayList<Integer>>) nextuse.clone());
        }
    }


    private class MachLine {
        String OP;
        String ASSIGN;
        String LEFT;
        String RIGHT;

        public HashSet<String> REF = new HashSet<String>();
        public HashSet<String> DEF = new HashSet<String>();

        public HashSet<String> Life_IN  = new HashSet<String>();
        public HashSet<String> Life_OUT = new HashSet<String>();

        public NextUse Next_IN  = new NextUse();
        public NextUse Next_OUT = new NextUse();

        // Constructor
        public MachLine(String op, String assign, String left, String right) {
            this.OP = OPMap.get(op);
            this.ASSIGN = assign;

            this.LEFT = left;
            this.RIGHT = right;

            DEF.add(this.ASSIGN);
            if (this.LEFT.charAt(0) != ('#'))
                REF.add(this.LEFT);
            if (this.RIGHT.charAt(0) != ('#'))
                REF.add(this.RIGHT);
        }
        public String toString() {
            String buff = "";
            buff += "// Life_IN  : " +  Life_IN.toString() +"\n";
            buff += "// Life_OUT : " +  Life_OUT.toString() +"\n";
            buff += "// Next_IN  : " +  Next_IN.toString() +"\n";
            buff += "// Next_OUT : " +  Next_OUT.toString() +"\n";
            return buff;
        }
    }

    private void compute_LifeVar() {
        // TODO: Implement LifeVariable algorithm on the CODE array (for basic bloc)
        CODE.get(CODE.size() - 1).Life_OUT = new HashSet<>(RETURNED);

        for (int i = CODE.size() - 1; i >= 0; i--) {
            if (i < CODE.size() - 1) {
                CODE.get(i).Life_OUT =  CODE.get(i + 1).Life_IN;
            }

            CODE.get(i).Life_IN = new HashSet<>(CODE.get(i).Life_OUT);
            CODE.get(i).Life_IN.removeAll(CODE.get(i).DEF);
            CODE.get(i).Life_IN.addAll(CODE.get(i).REF);
        }
    }

    private void compute_NextUse() {
        // TODO: Implement NextUse algorithm on the CODE array (for basic bloc)
        for (int i = CODE.size() - 1; i >= 0; i--) {
            if (i < CODE.size() - 1) {
                CODE.get(i).Next_OUT = (NextUse) CODE.get(i + 1).Next_IN.clone();
            }
            for (Map.Entry<String, ArrayList<Integer>> entry: CODE.get(i).Next_OUT.nextuse.entrySet()) {
                if (!CODE.get(i).DEF.contains(entry.getKey())) {
                    CODE.get(i).Next_IN.nextuse.put(entry.getKey(),(ArrayList<Integer>) entry.getValue().clone());
                }
            }

            for (String ref : CODE.get(i).REF) {
                CODE.get(i).Next_IN.add(ref, i);
            }
        }
    }


    // choose_register function: will be used in the print_machineCode function
    // returns the register assigned to var.
    // The assignation is done with respect to the life and next information.
    // The boolean load_if_not_found indicates if the variable needs to be loaded
    // from the memory to be accessible in REGISTERS
    public String choose_register(String var, HashSet<String> life, NextUse next, boolean load_if_not_found) {
        // /!\ TODO this function should generate the LD and ST when needed

        // TODO: if var is a constant (starts with '#'), return var
        if (var.startsWith("#")) {
            return var;
        }

        // TODO: if REGISTERS contains var, return "R"+index
        if (REGISTERS.contains(var)) {
           // return "R" + var;
        }

        // TODO: if REGISTERS size is not max (<REG), add var to REGISTERS and return "R"+index

        // TODO: if REGISTERS has max size,
        //          put var in space of an other variable which is not used anymore
        //          or
        //          put var in space of var which as the largest next-use

        return null;
    }

    public void print_machineCode() {
        // TODO: Print the machine code (this function needs to be change)
        for (int i = 0; i < CODE.size(); i++) { // print the output
            m_writer.println("// Step " + i);

            m_writer.println(CODE.get(i));
        }
    }


    public List<String> set_ordered(Set<String> s) {
        // function given to order a set in alphabetic order
        List<String> list = new ArrayList<String>(s);
        Collections.sort(list);
        return list;
    }

    // TODO: add any class you judge necessary, and explain them in the report. GOOD LUCK!
}
