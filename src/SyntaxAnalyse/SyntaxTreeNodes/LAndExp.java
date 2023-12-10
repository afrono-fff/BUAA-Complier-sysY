package SyntaxAnalyse.SyntaxTreeNodes;

import java.util.ArrayList;

public class LAndExp {
    // LAndExp → EqExp | LAndExp '&&' EqExp
    // 消除左递归： LAndExp → EqExp { '&&' EqExp }
    ArrayList<EqExp> eqExpList;
    public LAndExp(ArrayList<EqExp> eqExpList){
        this.eqExpList = eqExpList;
    }

    public ArrayList<EqExp> getEqExpList() {
        return eqExpList;
    }

    public void printSyntax(){
        System.out.println("<LAndExp>");
    }
}
