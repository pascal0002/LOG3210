package analyzer.visitors;

import analyzer.ast.*;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import sun.awt.Symbol;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.swing.text.StyledEditorKit;
import java.awt.*;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Vector;


/**
 * Created: 19-02-15
 * Last Changed: 20-10-6
 * Author: Félix Brunet & Doriane Olewicki
 *
 * Description: Ce visiteur explore l'AST et génère un code intermédiaire.
 */

public class IntermediateCodeGenVisitor implements ParserVisitor {

    //le m_writer est un Output_Stream connecter au fichier "result". c'est donc ce qui permet de print dans les fichiers
    //le code généré.
    private final PrintWriter m_writer;
    //m_writer.println(id + " : " + type +  " (" + c + ")");
    public IntermediateCodeGenVisitor(PrintWriter writer) {
        m_writer = writer;
    }
    public HashMap<String, VarType> SymbolTable = new HashMap<>();

    private int id = 0;
    private int label = 0;
    /*
    génère une nouvelle variable temporaire qu'il est possible de print
    À noté qu'il serait possible de rentrer en conflit avec un nom de variable définit dans le programme.
    Par simplicité, dans ce tp, nous ne concidérerons pas cette possibilité, mais il faudrait un générateur de nom de
    variable beaucoup plus robuste dans un vrai compilateur.
     */
    private String genId() {
        return "_t" + id++;
    }

    //génère un nouveau Label qu'il est possible de print.
    private String genLabel() {
        return "_L" + label++;
    }

    @Override
    public Object visit(SimpleNode node, Object data) {
        return data;
    }

    @Override
    public Object visit(ASTProgram node, Object data)  {
        String endLabel = genLabel();
        node.childrenAccept(this, endLabel);
        m_writer.println(endLabel);
        return null;
    }

    /*
    Code fournis pour remplir la table de symbole.
    Les déclarations ne sont plus utile dans le code à trois adresse.
    elle ne sont donc pas concervé.
     */
    @Override
    public Object visit(ASTDeclaration node, Object data) {
        ASTIdentifier id = (ASTIdentifier) node.jjtGetChild(0);
        VarType t;
        if(node.getValue().equals("bool")) {
            t = VarType.Bool;
        } else {
            t = VarType.Number;
        }
        SymbolTable.put(id.getValue(), t);
        return null;
    }

    @Override
    public Object visit(ASTBlock node, Object data) {
        if (node.jjtGetNumChildren() == 1) {
            node.jjtGetChild(0).jjtAccept(this, data);
        }

        else {
            for (int i = 0; i < node.jjtGetNumChildren(); i++) {
                if (i != node.jjtGetNumChildren() - 1) {
                    String lb = genLabel();
                    node.jjtGetChild(i).jjtAccept(this, lb);
                    m_writer.println(lb);
                }
                else {
                    node.jjtGetChild(i).jjtAccept(this, data);
                }
            }

        }

        return null;
    }

    @Override
    public Object visit(ASTStmt node, Object data) {

        node.childrenAccept(this, data);
        return null;
    }

    /*
    le If Stmt doit vérifier s'il à trois enfants pour savoir s'il s'agit d'un "if-then" ou d'un "if-then-else".
     */
    @Override
    public Object visit(ASTIfStmt node, Object data) {
        if (node.jjtGetNumChildren() == 2) {
            BoolLabel bl = new BoolLabel(genLabel(), (String)data);

            node.jjtGetChild(0).jjtAccept(this, bl);
            m_writer.println(bl.lTrue);
            node.jjtGetChild(1).jjtAccept(this, data);
        }
        else if (node.jjtGetNumChildren() == 3) {
            BoolLabel bl = new BoolLabel(genLabel(), genLabel());
            node.jjtGetChild(0).jjtAccept(this, bl);
            m_writer.println(bl.lTrue);
            node.jjtGetChild(1).jjtAccept(this, data);
            m_writer.println("goto " + data);
            m_writer.println(bl.lFalse);
            node.jjtGetChild(2).jjtAccept(this, data);
        }
        else {
            throw new NotImplementedException();
        }

        return null;
    }

    @Override
    public Object visit(ASTWhileStmt node, Object data) {
        String begin = genLabel();
        BoolLabel bl = new BoolLabel(genLabel(), (String)data);
        m_writer.println(begin);
        node.jjtGetChild(0).jjtAccept(this, bl);
        m_writer.println(bl.lTrue);
        node.jjtGetChild(1).jjtAccept(this, begin);
        m_writer.println("goto " + begin);

        return null;
    }


    @Override
    public Object visit(ASTAssignStmt node, Object data) {

        ASTIdentifier test = (ASTIdentifier)node.jjtGetChild(0);
        String id = test.getValue();

        if (SymbolTable.get(id) == VarType.Number) {
            String addr = (String)node.jjtGetChild(1).jjtAccept(this, data);
            m_writer.println(id + " = " + addr);
        }
        else {
            BoolLabel bl = new BoolLabel(genLabel(), genLabel());
            node.jjtGetChild(1).jjtAccept(this, bl);
            m_writer.println(bl.lTrue);
            m_writer.println(id + " = 1");
            m_writer.println("goto " + data);
            m_writer.println(bl.lFalse);
            m_writer.println(id + " = 0");
        }

        return id;
    }



    @Override
    public Object visit(ASTExpr node, Object data){
        return node.jjtGetChild(0).jjtAccept(this, data);
    }

    //Expression arithmétique
    /*
    Les expressions arithmétique add et mult fonctionne exactement de la même manière. c'est pourquoi
    il est plus simple de remplir cette fonction une fois pour avoir le résultat pour les deux noeuds.

    On peut bouclé sur "ops" ou sur node.jjtGetNumChildren(),
    la taille de ops sera toujours 1 de moins que la taille de jjtGetNumChildren
     */
    public Object codeExtAddMul(SimpleNode node, Object data, Vector<String> ops) {
        if (node.jjtGetNumChildren() == 1) {
            return node.jjtGetChild(0).jjtAccept(this, data);
        }

        String op = " " + ops.firstElement() + " ";
        String addr = "";
        String tmp = "";
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            if (i % 2 == 0) {
                addr = genId();
                String res = (String)node.jjtGetChild(i).jjtAccept(this, data);
                tmp += addr + " = " + res;
            }
            else {
                tmp += op + node.jjtGetChild(i).jjtAccept(this, data);
                m_writer.println(tmp);
                tmp = "";
            }
        }
        return addr;
    }

    @Override
    public Object visit(ASTAddExpr node, Object data) {
        return codeExtAddMul(node, data, node.getOps());
    }

    @Override
    public Object visit(ASTMulExpr node, Object data) {
        return codeExtAddMul(node, data, node.getOps());
    }

    //UnaExpr est presque pareil au deux précédente. la plus grosse différence est qu'il ne va pas
    //chercher un deuxième noeud enfant pour avoir une valeur puisqu'il s'agit d'une opération unaire.
    @Override
    public Object visit(ASTUnaExpr node, Object data) {
//        node.jjtGetChild(0).jjtAccept(this, data);
//        return null;
        Vector<String> operators = node.getOps();
        if (operators.isEmpty()) {
            return node.jjtGetChild(0).jjtAccept(this, data);
        }

        String addr = "";

        for (int i = 0; i < operators.size(); i++) {
            String tmp = "";
            String op = operators.get(i);
            if (i == 0) {
                String res = (String)node.jjtGetChild(0).jjtAccept(this, data);
                tmp = genId();
                m_writer.println(tmp + " = " + op + " " + res);
            }
            else{
                tmp = genId();
                m_writer.println(tmp + " = " + op + " " + addr);
            }
            addr = tmp;
        }

        return addr;
    }

    //expression logique
    @Override
    public Object visit(ASTBoolExpr node, Object data) {
        if (node.jjtGetNumChildren() == 1) {
            return node.jjtGetChild(0).jjtAccept(this, data);
        }

        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            if (i % 2 == 0) {
                String op = (i == 0) ? (String)node.getOps().get(0) : (String)node.getOps().get(i - 1);
                if (op.equals("&&")) {
                    BoolLabel bl = new BoolLabel(genLabel(), ((BoolLabel)data).lFalse);
                    node.jjtGetChild(i).jjtAccept(this, bl);
                    m_writer.println(bl.lTrue);
                }
                else if (op.equals("||")) {
                    BoolLabel bl = new BoolLabel(((BoolLabel)data).lTrue, genLabel());
                    node.jjtGetChild(i).jjtAccept(this, bl);
                    m_writer.println(bl.lFalse);
                }
            }
            else {
                node.jjtGetChild(i).jjtAccept(this, data);
            }
        }

        return null;
    }





    @Override
    public Object visit(ASTCompExpr node, Object data) {
        if (node.jjtGetNumChildren() == 1) {
            return node.jjtGetChild(0).jjtAccept(this, data);
        }

        m_writer.println("if " + node.jjtGetChild(0).jjtAccept(this, data) + " " + node.getValue() + " "
                         + node.jjtGetChild(1).jjtAccept(this, data) + " goto " + ((BoolLabel)data).lTrue
                        );
        m_writer.println("goto " + ((BoolLabel)data).lFalse);

        return null;
    }


    /*
    Même si on peut y avoir un grand nombre d'opération, celle-ci s'annullent entre elle.
    il est donc intéressant de vérifier si le nombre d'opération est pair ou impaire.
    Si le nombre d'opération est pair, on peut simplement ignorer ce noeud.
     */
    @Override
    public Object visit(ASTNotExpr node, Object data) {
        if (node.getOps().size() % 2 == 0) {
            return node.jjtGetChild(0).jjtAccept(this, data);
        }
        else {
            BoolLabel bl = new BoolLabel(((BoolLabel)data).lFalse, ((BoolLabel)data).lTrue);
            return node.jjtGetChild(0).jjtAccept(this, bl);
        }


    }

    @Override
    public Object visit(ASTGenValue node, Object data) {
        return node.jjtGetChild(0).jjtAccept(this, data);
    }

    /*
    BoolValue ne peut pas simplement retourné sa valeur à son parent contrairement à GenValue et IntValue,
    Il doit plutôt généré des Goto direct, selon sa valeur.
     */
    @Override
    public Object visit(ASTBoolValue node, Object data) {
        if (node.getValue() == true) {
            m_writer.println("goto " + ((BoolLabel)data).lTrue);
        }
        else {
            m_writer.println("goto " + ((BoolLabel)data).lFalse);
        }

        return null;
    }


    /*
    si le type de la variable est booléenne, il faudra généré des goto ici.
    le truc est de faire un "if value == 1 goto Label".
    en effet, la structure "if valeurBool goto Label" n'existe pas dans la syntaxe du code à trois adresse.
     */
    @Override
    public Object visit(ASTIdentifier node, Object data) {
        if (SymbolTable.get(node.getValue()) == VarType.Bool) {
            m_writer.println("if " + node.getValue() + " == 1 goto " + ((BoolLabel)data).lTrue);
            m_writer.println("goto " + ((BoolLabel)data).lFalse);
        }

        return node.getValue();
    }

    @Override
    public Object visit(ASTIntValue node, Object data) {
        return Integer.toString(node.getValue());
    }


    @Override
    public Object visit(ASTSwitchStmt node, Object data) {

        String begin = genLabel();
        m_writer.println("goto " + begin);

        String switchVar = (String)node.jjtGetChild(0).jjtAccept(this, data);

        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            m_writer.println(genLabel());
            node.jjtGetChild(i).jjtAccept(this, data);
            m_writer.println("goto " + data);
        }


        //BoolLabel bl = new BoolLabel(genLabel(), (String)data);
        //node.jjtGetChild(0).jjtAccept(this, bl);


        return null;
    }

    @Override
    public Object visit(ASTCaseStmt node, Object data) {
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).jjtAccept(this, data);
        }
        return null;
    }

    @Override
    public Object visit(ASTDefaultStmt node, Object data) {
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).jjtAccept(this, data);
        }
        return null;
    }

    //des outils pour vous simplifier la vie et vous enligner dans le travail
    public enum VarType {
        Bool,
        Number
    }

    //utile surtout pour envoyé de l'informations au enfant des expressions logiques.
    private class BoolLabel {
        public String lTrue = null;
        public String lFalse = null;

        public BoolLabel(String t, String f) {
            lTrue = t;
            lFalse = f;
        }
    }


}
