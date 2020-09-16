package analyzer.visitors;

import analyzer.SemantiqueError;
import analyzer.ast.*;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created: 19-01-10
 * Last Changed: 19-01-25
 * Author: Félix Brunet
 * <p>
 * Description: Ce visiteur explorer l'AST est renvois des erreur lorqu'une erreur sémantique est détecté.
 */

public class SemantiqueVisitor implements ParserVisitor {

    private final PrintWriter m_writer;

    private HashMap<String, VarType> SymbolTable = new HashMap<>(); // mapping variable -> type

    // variable pour les metrics
    public int VAR = 0;
    public int WHILE = 0;
    public int IF = 0;
    public int OP = 0;

    public SemantiqueVisitor(PrintWriter writer) {
        m_writer = writer;
    }

    /*
    Le Visiteur doit lancer des erreurs lorsqu'un situation arrive.

    pour vous aider, voici le code a utilisé pour lancer les erreurs

    //utilisation d'identifiant non défini
    throw new SemantiqueError("Invalid use of undefined Identifier " + node.getValue());

    //utilisation de nombre dans la condition d'un if ou d'un while
    throw new SemantiqueError("Invalid type in condition");

    //assignation d'une valeur a une variable qui a déjà recu une valeur d'un autre type
    ex : a = 1; a = true;
    throw new SemantiqueError("Invalid type in assignment");

    //expression invalide : (note, le code qui l'utilise est déjà fournis)
    throw new SemantiqueError("Invalid type in expression got " + type.toString() + " was expecting " + expectedType);

     */

    @Override
    public Object visit(SimpleNode node, Object data) {
        return data;
    }

    @Override
    public Object visit(ASTProgram node, Object data) {
        node.childrenAccept(this, data);
        m_writer.print(String.format("{VAR:%d, WHILE:%d, IF:%d, OP:%d}", this.VAR, this.WHILE, this.IF, this.OP));
        return null;
    }

    /*
    Doit enregistrer les variables avec leur type dans la table symbolique.
     */
    @Override
    public Object visit(ASTDeclaration node, Object data) {
        this.VAR++;
        String varName = ((ASTIdentifier) node.jjtGetChild(0)).getValue();
        if (SymbolTable.containsKey(varName)) {
            throw new SemantiqueError(String.format("Identifier %s has multiple declarations.", varName));
        }
        SymbolTable.put(varName, node.getValue().equals("num") ? VarType.Number : VarType.Bool);

        return null;
    }

    @Override
    public Object visit(ASTBlock node, Object data) {
        node.childrenAccept(this, data);
        return null;
    }


    @Override
    public Object visit(ASTStmt node, Object data) {
        node.childrenAccept(this, data);
        return null;
    }

//    private void callChildenCond(SimpleNode node) {
//        DataStruct d = new DataStruct();
//        node.jjtGetChild(0).jjtAccept(this, d);
//        int numChildren = node.jjtGetNumChildren();
//        for (int i = 1; i < numChildren; i++) {
//            node.jjtGetChild(i).jjtAccept(this, data);
//        }
//    }

    /*
    les structures conditionnelle doivent vérifier que leur expression de condition est de type booléenne
    On doit aussi compter les conditions dans les variables IF et WHILE
     */
    @Override
    public Object visit(ASTIfStmt node, Object data) {
        this.IF++;
        DataStruct d = new DataStruct();
        node.jjtGetChild(0).jjtAccept(this, d);
        int numChildren = node.jjtGetNumChildren();
        for (int i = 1; i < numChildren; i++) {
            node.jjtGetChild(i).jjtAccept(this, data);
        }
        return null;
    }

    @Override
    public Object visit(ASTWhileStmt node, Object data) {
        this.WHILE++;
        DataStruct d = new DataStruct();
        node.jjtGetChild(0).jjtAccept(this, d);
        int numChildren = node.jjtGetNumChildren();
        for (int i = 1; i < numChildren; i++) {
            node.jjtGetChild(i).jjtAccept(this, data);
        }
        return null;
    }

    /*
    On doit vérifier que le type de la variable est compatible avec celui de l'expression.
     */
    @Override
    public Object visit(ASTAssignStmt node, Object data) {
        String varName = ((ASTIdentifier) node.jjtGetChild(0)).getValue();
        DataStruct d = new DataStruct();
        node.jjtGetChild(1).jjtAccept(this, d);
        //Invalid type in assignation of Identifier b
        if (d.type != SymbolTable.get(varName)) {
            throw new SemantiqueError(String.format("Invalid type in assignation of Identifier %s", varName));
        }

        return null;
    }

    @Override
    public Object visit(ASTExpr node, Object data) {
        //Il est normal que tous les noeuds jusqu'à expr retourne un type.

        node.childrenAccept(this, data);
        return null;
    }

    @Override
    public Object visit(ASTCompExpr node, Object data) {
        /*attention, ce noeud est plus complexe que les autres.
        si il n'a qu'un seul enfant, le noeud a pour type le type de son enfant.

        si il a plus d'un enfant, alors ils s'agit d'une comparaison. il a donc pour type "Bool".

        de plus, il n'est pas acceptable de faire des comparaisons de booleen avec les opérateur < > <= >=.
        les opérateurs == et != peuvent être utilisé pour les nombres et les booléens, mais il faut que le type soit le même
        des deux côté de l'égalité/l'inégalité.
        */
        int numChildren = node.jjtGetNumChildren();
        VarType equalityTypes[] = new VarType[2];

        if (numChildren > 1) {
            this.OP++;
        }

        for (int i = 0; i < numChildren; i++) {
            DataStruct d = new DataStruct();
            node.jjtGetChild(i).jjtAccept(this, d);

            if (numChildren == 1) {
                ((DataStruct)data).type = d.type;
            }
            else if (numChildren > 1) {
                String operator = node.getValue();
                if (operator.equals(">") || operator.equals("<") || operator.equals(">=") || operator.equals("<=")) {
                    if (d.type == VarType.Bool) {
                        throw new SemantiqueError("Invalid type in expression");
                    }
                }
                else if (operator.equals("==") || operator.equals("!=")) {
                    equalityTypes[i] = d.type;
                    if (i == 1 && equalityTypes[0] != equalityTypes[1]) {
                        throw new SemantiqueError("Invalid type in expression");
                    }
                }
                else {
                    throw new SemantiqueError("Shouldn't fall here");
                }
                // Comparison always returns a bool
                ((DataStruct)data).type = VarType.Bool;
            }
        }



        return null;
    }


    /*
    opérateur binaire
    si il n'y a qu'un enfant, aucune vérification à faire.
    par exemple, un AddExpr peut retourné le type "Bool" à condition de n'avoir qu'un seul enfant.
     */
    @Override
    public Object visit(ASTAddExpr node, Object data) {
        int numChildren = node.jjtGetNumChildren();

        if (numChildren > 1) {
            this.OP++;
        }

        for (int i = 0; i < numChildren; i++) {
            DataStruct d = new DataStruct();
            node.jjtGetChild(i).jjtAccept(this, d);

            if (numChildren > 1 && d.type == VarType.Bool) {
                throw new SemantiqueError("Invalid type in expression");
            }

            if (d.type != null) {
                ((DataStruct)data).type = d.type;
            }
        }
        return null;
    }

    @Override
    public Object visit(ASTMulExpr node, Object data) {
        int numChildren = node.jjtGetNumChildren();

        if (numChildren > 1) {
            this.OP++;
        }

        for (int i = 0; i < numChildren; i++) {
            DataStruct d = new DataStruct();
            node.jjtGetChild(i).jjtAccept(this, d);

            if (numChildren > 1 && d.type == VarType.Bool) {
                throw new SemantiqueError("Invalid type in expression");
            }

            if (d.type != null) {
                ((DataStruct)data).type = d.type;
            }
        }
        return null;
    }

    @Override
    public Object visit(ASTBoolExpr node, Object data) {
        int numChildren = node.jjtGetNumChildren();

        if (numChildren > 1) {
            this.OP++;
        }

        for (int i = 0; i < numChildren; i++) {
            DataStruct d = new DataStruct();
            node.jjtGetChild(i).jjtAccept(this, d);
            if (numChildren == 1) {
                ((DataStruct)data).type = d.type;
            }

            else if (numChildren > 1) {

                if (d.type == VarType.Number) {
                    throw new SemantiqueError("Invalid type in expression");
                }
                // BoolExpr result is always a bool
                ((DataStruct)data).type = VarType.Bool;
            }

        }

        return null;
    }

    /*
    opérateur unaire
    les opérateur unaire ont toujours un seul enfant.

    Cependant, ASTNotExpr et ASTUnaExpr ont la fonction "getOps()" qui retourne un vecteur contenant l'image (représentation str)
    de chaque token associé au noeud.

    Il est utile de vérifier la longueur de ce vecteur pour savoir si une opérande est présente.

    si il n'y a pas d'opérande, ne rien faire.
    si il y a une (ou plus) opérande, ils faut vérifier le type.

    */
    @Override
    public Object visit(ASTNotExpr node, Object data) {
        node.jjtGetChild(0).jjtAccept(this, data);
        if (node.getOps().size() > 0) {
            this.OP++;
            if (((DataStruct)data).type == VarType.Number) {
                throw new SemantiqueError("Invalid type in expression");
            }
        }

        return null;
    }

    @Override
    public Object visit(ASTUnaExpr node, Object data) {
        node.jjtGetChild(0).jjtAccept(this, data);
        if (node.getOps().size() > 0) {
            this.OP++;
            if (((DataStruct) data).type == VarType.Bool) {
                throw new SemantiqueError("Invalid type in expression");
            }
        }
        return null;
    }

    /*
    les noeud ASTIdentifier aillant comme parent "GenValue" doivent vérifier leur type.

    Ont peut envoyé une information a un enfant avec le 2e paramètre de jjtAccept ou childrenAccept.
     */
    @Override
    public Object visit(ASTGenValue node, Object data) {
        node.childrenAccept(this, data);
        return null;
    }


    @Override
    public Object visit(ASTBoolValue node, Object data) {
        ((DataStruct) data).type = VarType.Bool;
        return null;
    }

    @Override
    public Object visit(ASTIdentifier node, Object data) {

        if (node.jjtGetParent() instanceof ASTGenValue) {
            String varName = node.getValue();
            VarType varType = SymbolTable.get(varName);

            ((DataStruct) data).type = varType;
        }

        return null;
    }

    @Override
    public Object visit(ASTIntValue node, Object data) {
        ((DataStruct) data).type = VarType.Number;
        return null;
    }


    //des outils pour vous simplifier la vie et vous enligner dans le travail
    public enum VarType {
        Bool,
        Number
    }


    private class DataStruct {
        public VarType type;

        public DataStruct() {
        }

        public DataStruct(VarType p_type) {
            type = p_type;
        }

    }
}
