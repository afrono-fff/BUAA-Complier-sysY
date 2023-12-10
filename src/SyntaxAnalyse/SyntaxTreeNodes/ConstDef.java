package SyntaxAnalyse.SyntaxTreeNodes;

import LexicalAnalyse.Word;

import java.util.ArrayList;

public class ConstDef {
    // ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal
    private Word ident;
    private ArrayList<ConstExp> constExpList;
    private ConstInitVal constInitVal;
    public ConstDef(Word ident, ArrayList<ConstExp> constExpList, ConstInitVal constInitVal){
        this.ident = ident;
        this.constExpList = constExpList;
        this.constInitVal = constInitVal;
    }

    public Word getIdent() {
        return ident;
    }

    public ArrayList<ConstExp> getConstExpList() {
        return constExpList;
    }

    public ConstInitVal getConstInitVal() {
        return constInitVal;
    }

    public void printSyntax(){
        System.out.println("<ConstDef>");
    }
}
