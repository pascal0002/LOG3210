package analyzer.visitors;

import analyzer.SemantiqueError;
import analyzer.ast.*;

import javax.xml.crypto.Data;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;


/**
 * Created: 19-01-10
 * Last Changed: 19-01-25
 * Author: Félix Brunet
 *
 * Description: Ce visiteur explorer l'AST est renvoie des exceptions lorqu'une erreur sémantique est détecté.
 */

public class SemantiqueVisitor implements ParserVisitor {

    private final PrintWriter m_writer;

    public HashMap<String, VarType> SymbolTable = new HashMap<>();

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
    public Object visit(ASTProgram node, Object data)  {
        node.childrenAccept(this, data);
        m_writer.print("all good");
        return data;
    }

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
        node.childrenAccept(this, data);
        return data;
    }


    @Override
    public Object visit(ASTStmt node, Object data) {
        node.childrenAccept(this, data);
        return data;
    }

    @Override
    public Object visit(ASTIfStmt node, Object data) {
        DataStruct firstD = (DataStruct) node.jjtGetChild(0).jjtAccept(this, data);
        if(!estCompatible(firstD.type, VarType.Bool)) {
            throw new SemantiqueError("Invalid type in condition");
        }
        for(int i = 1; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).jjtAccept(this, data);
        }

        return data;
    }

    @Override
    public Object visit(ASTWhileStmt node, Object data) {
        DataStruct firstD = (DataStruct) node.jjtGetChild(0).jjtAccept(this, data);
        if(!estCompatible(firstD.type, VarType.Bool)) {
            throw new SemantiqueError("Invalid type in condition");
        }
        for(int i = 1; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).jjtAccept(this, data);
        }

        return data;
    }


    @Override
    public Object visit(ASTAssignStmt node, Object data) {
        DataStruct assignId = (DataStruct) node.jjtGetChild(0).jjtAccept(this, data);
        DataStruct assignExpr = (DataStruct) node.jjtGetChild(1).jjtAccept(this, data);
        if(!estCompatible(assignId.type, assignExpr.type)) {
            throw new SemantiqueError("Invalid type in assignment");
        }
        return data;
    }

    @Override
    public Object visit(ASTExpr node, Object data){
        return node.jjtGetChild(0).jjtAccept(this, data);
    }

    @Override
    public Object visit(ASTBoolExpr node, Object data) {

        return visitExprAst(node, data, VarType.Bool);
    }

    @Override
    public Object visit(ASTCompExpr node, Object data) {
        DataStruct firstD = (DataStruct) node.jjtGetChild(0).jjtAccept(this, data);
        String op = node.getValue();

        VarType expectedType = VarType.Number;

        VarType newType = firstD.type;
        if(op != null) {
            if(op.equals("==") || op.equals("!=")) {
                expectedType = firstD.type;

            }
            newType = VarType.Bool;
        }

        for(int i = 1; i < node.jjtGetNumChildren(); i++) {
            DataStruct d = (DataStruct) node.jjtGetChild(i).jjtAccept(this, data);
            firstD.checkType(d, expectedType);
        }
        firstD.type = newType;
        return firstD;
    }

    @Override
    public Object visit(ASTAddExpr node, Object data) {
        return visitExprAst(node, data, VarType.Number);
    }

    @Override
    public Object visit(ASTMulExpr node, Object data) {
        return visitExprAst(node, data, VarType.Number);
    }


    //Unary operator
    @Override
    public Object visit(ASTNotExpr node, Object data) {
        Boolean haveOp = node.getOps().size() > 0;

        DataStruct firstD = (DataStruct) node.jjtGetChild(0).jjtAccept(this, data);

        if(haveOp) {
            firstD.checkType(VarType.Bool);
        }
        return firstD;
    }

    @Override
    public Object visit(ASTUnaExpr node, Object data) {
        Boolean haveOp = node.getOps().size() > 0;

        DataStruct firstD = (DataStruct) node.jjtGetChild(0).jjtAccept(this, data);

        if(haveOp) {
            firstD.checkType(VarType.Number);
        }
        return firstD;
    }

    private DataStruct visitExprAst(SimpleNode node, Object data, VarType expectedType) {
        DataStruct firstD = (DataStruct) node.jjtGetChild(0).jjtAccept(this, data);

        for(int i = 1; i < node.jjtGetNumChildren(); i++) {
            DataStruct d = (DataStruct) node.jjtGetChild(i).jjtAccept(this, data);
            firstD.checkType(d, expectedType);
        }
        return firstD;
    }



    @Override
    public Object visit(ASTGenValue node, Object data) {
        return node.jjtGetChild(0).jjtAccept(this, data);
    }


    @Override
    public Object visit(ASTBoolValue node, Object data) {
        DataStruct d = new DataStruct(VarType.Bool);
        return d;
    }


    @Override
    public Object visit(ASTIdentifier node, Object data) {
        DataStruct d = new DataStruct();

        if(data == null || !data.equals("declaration")) {
            if(SymbolTable.get(node.getValue()) != null) {
                d.type = SymbolTable.get(node.getValue());
            } else {
                throw new SemantiqueError("Invalid use of undefined Identifier " + node.getValue());
            }
        }
        return d;
    }

    @Override
    public Object visit(ASTIntValue node, Object data) {
        return new DataStruct(VarType.Number);
    }

    @Override
    public Object visit(ASTSwitchStmt node, Object data) {

        node.childrenAccept(this, null);
        return null;
    }

    @Override
    public Object visit(ASTCaseStmt node, Object data) {

        node.childrenAccept(this, null);
        return null;
    }

    @Override
    public Object visit(ASTDefaultStmt node, Object data) {

        node.childrenAccept(this, null);
        return null;
    }

    //des outils pour vous simplifier la vie et vous enligner dans le travail
    public enum VarType {
        Bool,
        Number
    }

    private boolean estCompatible(VarType a, VarType b) {
        return a == b;
    }

    private class DataStruct {
        public VarType type;

        public DataStruct() {}

        public DataStruct(VarType p_type){
            type = p_type;
        }

        public void checkType(VarType expectedType) {
            if(!estCompatible(type, expectedType)) {
                throw new SemantiqueError("Invalid type in expression got " + type.toString() + " was expecting " + expectedType);
            }
        }

        public void checkType(DataStruct d, VarType expectedType) {
            if(!estCompatible(type, expectedType) || !estCompatible(d.type, expectedType)) {
                throw new SemantiqueError("Invalid type in expression got " + type.toString() + " and " + d.type.toString() + " was expecting " + expectedType);
            }
        }
    }
}
