package SyntaxAnalyse.SyntaxTreeNodes;

import java.util.ArrayList;

public class ConstDecl {
    private BType bType;
    private ArrayList<ConstDef> constDefList;
    public ConstDecl(BType bType, ArrayList<ConstDef> constDefList){
        this.bType = bType;
        this.constDefList = constDefList;
    }

    public ArrayList<ConstDef> getConstDefList() {
        return constDefList;
    }

    public BType getbType() {
        return bType;
    }

    public void printSyntax(){
        System.out.println("<ConstDecl>");
    }
}
