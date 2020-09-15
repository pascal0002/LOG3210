/* Generated By:JJTree: Do not edit this line. ASTBoolExpr.java Version 7.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package analyzer.ast;

import java.util.Vector;

public class ASTBoolExpr extends SimpleNode {
  public ASTBoolExpr(int id) {
    super(id);
  }

  public ASTBoolExpr(Parser p, int id) {
    super(p, id);
  }


    /** Accept the visitor. **/
    public Object jjtAccept(ParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }

    // PLB
    private Vector<String> m_ops = new Vector<>();
    public void addOp(String o) { m_ops.add(o); }
    public Vector getOps() { return m_ops; }

}
/* JavaCC - OriginalChecksum=9de7ce665b48309618ff4d7aaa9aa4ac (do not edit this line) */
