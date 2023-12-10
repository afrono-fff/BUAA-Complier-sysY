package SyntaxAnalyse.SyntaxTreeNodes;

import java.util.ArrayList;

public class VarDecl {
    private BType bType;
    private ArrayList<VarDef> varDefList;
    public VarDecl(BType bType, ArrayList<VarDef> varDefList){
        this.bType = bType;
        this.varDefList = varDefList;
    }

    public ArrayList<VarDef> getVarDefList() {
        return varDefList;
    }

    public void printSyntax(){
        System.out.println("<VarDecl>");
    }
}
