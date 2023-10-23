package SyntaxAnalyse.SyntaxTreeNodes;

public class Exp {
    //  Exp → AddExp
    private AddExp addExp;
    public Exp(AddExp addExp){
        this.addExp = addExp;
    }
    public void printSyntax(){
        System.out.println("<Exp>");
    }
    public int getDimension(){
        return addExp.getDimension();
    }
}
