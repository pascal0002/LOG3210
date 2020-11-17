package analyzer.visitors;

import analyzer.ast.ASTProgram;
import analyzer.ast.Node;

import java.io.PrintWriter;

/**
 * Created: 17-08-02
 * Last Changed: 19-01-10
 * Author: Nicolas Cloutier
 *
 * Description: This visitor explore the AST
 * and "pretty" print the code.
 */

public class PrintAllVisitor extends AbstractVisitor {

    private final PrintWriter m_writer;

    private String m_indent;

    public PrintAllVisitor(PrintWriter writer) {
        m_writer = writer;
        m_indent = "";
    }

    private void moreIndent() {
        m_indent += "| ";
    }

    private void lessIndent() {
        m_indent = m_indent.substring(0, m_indent.length() - 2);
    }

    private void printAndRecurse(Node node) {
        m_writer.print(m_indent);
        String[] name = node.getClass().getName().split("\\.");

        m_writer.println(name[name.length -1]);
        moreIndent();
        for(int i = 0; i < node.jjtGetNumChildren(); i++) {
            printAndRecurse(node.jjtGetChild(i));
        }
        lessIndent();
    }

    @Override
    public Object visit(ASTProgram node, Object data) {
        printAndRecurse(node);
        return null;
    }
}
