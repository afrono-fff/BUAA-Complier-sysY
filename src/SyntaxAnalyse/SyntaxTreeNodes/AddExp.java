package SyntaxAnalyse.SyntaxTreeNodes;

import java.util.ArrayList;

public class AddExp {
    // AddExp → MulExp | AddExp ('+' | '−') MulExp
    // 消除左递归： AddExp → MulExp { ('+' | '−') MulExp }
    private ArrayList<MulExp> mulExpList;
    private ArrayList<String> opList; // 操作符: plus  minu
    public AddExp(ArrayList<MulExp> mulExpList, ArrayList<String> opList){
        this.mulExpList = mulExpList;
        this.opList = opList;
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

    public ArrayList<MulExp> getMulExpList() {
        return mulExpList;
    }

    public ArrayList<String> getOpList() {
        return opList;
    }
}
