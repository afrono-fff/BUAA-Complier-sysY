package SyntaxAnalyse.SyntaxTreeNodes;

import java.util.ArrayList;

public class ConstInitVal {
    // ConstInitVal → ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
    private ConstExp constExp;
    private ArrayList<ConstInitVal> constInitValList;
    public ConstInitVal(ArrayList<ConstInitVal> constInitValList){
        this.constExp = null;
        this.constInitValList = constInitValList;
    }
    public ConstInitVal(ConstExp constExp){
        this.constExp = constExp;
        this.constInitValList = null;
    }
    public ConstInitVal(){
        super();
    }
    public void printSyntax(){
        System.out.println("<ConstInitVal>");
    }
}
