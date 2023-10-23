package SyntaxAnalyse.SyntaxTreeNodes;

import java.util.ArrayList;

public class RelExp {
    // RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
    // 消除左递归： RelExp → AddExp { ('<' | '>' | '<=' | '>=') AddExp }
    ArrayList<AddExp> addExpList;
    public RelExp(ArrayList<AddExp> addExpList){
        this.addExpList = addExpList;
    }
    public void printSyntax(){
        System.out.println("<RelExp>");
    }
}
