package SyntaxAnalyse.SyntaxTreeNodes;

import java.util.ArrayList;

public class RelExp {
    // RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
    // 消除左递归： RelExp → AddExp { ('<' | '>' | '<=' | '>=') AddExp }
    ArrayList<AddExp> addExpList;
    ArrayList<String> opList;
    public RelExp(ArrayList<AddExp> addExpList, ArrayList<String> opList){
        this.addExpList = addExpList;
        this.opList = opList;
    }

    public ArrayList<AddExp> getAddExpList() {
        return addExpList;
    }

    public ArrayList<String> getOpList() {
        return opList;
    }

    public void printSyntax(){
        System.out.println("<RelExp>");
    }
}
