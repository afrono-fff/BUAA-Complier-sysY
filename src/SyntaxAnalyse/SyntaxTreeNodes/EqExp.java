package SyntaxAnalyse.SyntaxTreeNodes;

import java.util.ArrayList;

public class EqExp {
    // EqExp → RelExp | EqExp ('==' | '!=') RelExp
    // 消除左递归： EqExp → RelExp { ('==' | '!=') RelExp }
    ArrayList<RelExp> relExpList;
    ArrayList<String> opList;
    public EqExp(ArrayList<RelExp> relExpList, ArrayList<String> opList){
        this.relExpList = relExpList;
        this.opList = opList;
    }

    public ArrayList<String> getOpList() {
        return opList;
    }

    public ArrayList<RelExp> getRelExpList() {
        return relExpList;
    }

    public void printSyntax(){
        System.out.println("<EqExp>");
    }
}
