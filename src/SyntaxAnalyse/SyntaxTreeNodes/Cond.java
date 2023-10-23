package SyntaxAnalyse.SyntaxTreeNodes;

public class Cond {
    // Cond → LOrExp
    private LOrExp lOrExp;
    public Cond(LOrExp lOrExp){
        this.lOrExp = lOrExp;
    }
    public void printSyntax(){
        System.out.println("<Cond>");
    }
}
