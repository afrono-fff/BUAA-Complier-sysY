package SyntaxAnalyse.SyntaxTreeNodes;

public class ConstExp{
    // ConstExp → AddExp
    private AddExp addExp;
    public ConstExp(AddExp addExp){
        this.addExp = addExp;
    }

    public AddExp getAddExp() {
        return addExp;
    }

    public void printSyntax(){
        System.out.println("<ConstExp>");
    }
}
