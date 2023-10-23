package SyntaxAnalyse.SyntaxTreeNodes;

import java.util.ArrayList;

public class LOrExp {
    // LOrExp → LAndExp | LOrExp '||' LAndExp
    // 消除左递归： LOrExp → LAndExp { '||' LAndExp }
    private ArrayList<LAndExp> lAndExpList;
    public LOrExp(ArrayList<LAndExp> lAndExpList){
        this.lAndExpList = lAndExpList;
    }
    public void printSyntax(){
        System.out.println("<LOrExp>");
    }
}
