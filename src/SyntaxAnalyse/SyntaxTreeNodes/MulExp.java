package SyntaxAnalyse.SyntaxTreeNodes;

import java.util.ArrayList;

public class MulExp {
    // MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
    // 消除左递归：MulExp → UnaryExp { ('*' | '/' | '%') UnaryExp }
    private ArrayList<UnaryExp> unaryExpList;
    public MulExp(ArrayList<UnaryExp> unaryExpList){
        this.unaryExpList = unaryExpList;
    }
    public void printSyntax(){
        System.out.println("<MulExp>");
    }
    public int getDimension(){
        int dimension = -1;
        for(UnaryExp ue:unaryExpList){
            if(ue.getDimension() > dimension){
                dimension = ue.getDimension();
            }
            if(ue.getDimension() == -1){
                return -1;
            }
        }
        return dimension;
    }
}
