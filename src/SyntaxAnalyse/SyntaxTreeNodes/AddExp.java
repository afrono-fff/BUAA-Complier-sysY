package SyntaxAnalyse.SyntaxTreeNodes;

import java.util.ArrayList;

public class AddExp {
    // AddExp → MulExp | AddExp ('+' | '−') MulExp
    // 消除左递归： AddExp → MulExp { ('+' | '−') MulExp }
    private ArrayList<MulExp> mulExpList;
    public AddExp(ArrayList<MulExp> mulExpList){
        this.mulExpList = mulExpList;
    }
    public void printSyntax(){
        System.out.println("<AddExp>");
    }
    public int getDimension(){
        int maxDimension = -1; // 对于AddExp，其维数就是每一项MulExp的最大维数
        for(MulExp me: mulExpList){
            if(me.getDimension() > maxDimension){
                maxDimension = me.getDimension();
            }
        }
        return maxDimension;
    }
}
