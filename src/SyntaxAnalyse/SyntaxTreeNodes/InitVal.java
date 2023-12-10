package SyntaxAnalyse.SyntaxTreeNodes;

import java.util.ArrayList;

public class InitVal {
    // InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'
    private Exp exp;
    private ArrayList<InitVal> InitValList;
    private boolean isExp = false;
    public InitVal(Exp exp){ // 选Exp
        this.exp = exp;
        this.InitValList = null;
        isExp = true;
    }
    public InitVal(){ // 选右边且[]不选
        this.exp = null;
        this.InitValList = null;
    }
    public InitVal(ArrayList<InitVal> InitValList){
        this.InitValList = InitValList;
        this.exp = null;
    }

    public Exp getExp() {
        return exp;
    }

    public ArrayList<InitVal> getInitValList() {
        return InitValList;
    }

    public boolean isExp() {
        return isExp;
    }

    public void printSyntax(){
        System.out.println("<InitVal>");
    }

}
