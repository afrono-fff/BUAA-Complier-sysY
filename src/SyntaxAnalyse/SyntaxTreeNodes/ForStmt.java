package SyntaxAnalyse.SyntaxTreeNodes;

public class ForStmt {
    // ForStmt → LVal '=' Exp
    private LVal lVal;
    private Exp exp;
    public ForStmt(LVal lVal, Exp exp){
        this.lVal = lVal;
        this.exp = exp;
    }

    public Exp getExp() {
        return exp;
    }

    public LVal getlVal() {
        return lVal;
    }

    public void printSyntax(){
        System.out.println("<ForStmt>");
    }
}
