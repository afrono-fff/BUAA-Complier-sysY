package SyntaxAnalyse.SyntaxTreeNodes;

import java.util.ArrayList;

public class InitVal {
    // InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'
    private Exp exp;
    private ArrayList<InitVal> InitValList;
    public InitVal(Exp exp){ // 选Exp
        this.exp = exp;
        this.InitValList = null;
    }
    public InitVal(){ // 选右边且[]不选
        this.exp = null;
        this.InitValList = null;
    }
    public InitVal(ArrayList<InitVal> InitValList){
        this.InitValList = InitValList;
        this.exp = null;
    }
    public void printSyntax(){
        System.out.println("<InitVal>");
    }
}
