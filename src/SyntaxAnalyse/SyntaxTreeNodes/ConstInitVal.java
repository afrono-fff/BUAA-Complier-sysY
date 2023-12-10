package SyntaxAnalyse.SyntaxTreeNodes;

import java.util.ArrayList;

public class ConstInitVal {
    // ConstInitVal → ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
    private ConstExp constExp;
    private ArrayList<ConstInitVal> constInitValList;
    private boolean isConstExp = false;
    public ConstInitVal(ArrayList<ConstInitVal> constInitValList){
        this.constExp = null;
        this.constInitValList = constInitValList;
    }
    public ConstInitVal(ConstExp constExp){
        this.constExp = constExp;
        this.constInitValList = null;
        isConstExp = true;
    }
    public ConstInitVal(){
        super();
    }
    public void printSyntax(){
        System.out.println("<ConstInitVal>");
    }

    public ArrayList<ConstInitVal> getConstInitValList() {
        return constInitValList;
    }

    public ConstExp getConstExp() {
        return constExp;
    }

    public boolean isConstExp() {
        return isConstExp;
    }
}
