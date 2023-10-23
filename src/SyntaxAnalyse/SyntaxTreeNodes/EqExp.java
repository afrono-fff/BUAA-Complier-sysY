package SyntaxAnalyse.SyntaxTreeNodes;

import java.util.ArrayList;

public class EqExp {
    // EqExp → RelExp | EqExp ('==' | '!=') RelExp
    // 消除左递归： EqExp → RelExp { ('==' | '!=') RelExp }
    ArrayList<RelExp> relExpList;
    public EqExp(ArrayList<RelExp> relExpList){
        this.relExpList = relExpList;
    }
    public void printSyntax(){
        System.out.println("<EqExp>");
    }
}
