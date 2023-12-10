package SyntaxAnalyse.SyntaxTreeNodes;

import LexicalAnalyse.Word;

import java.util.ArrayList;

public class VarDef {
    // VarDef → Ident { '[' ConstExp ']' } | Ident { '[' ConstExp ']' } '=' InitVal
    // 提取左因子： VarDef  → Ident { '[' ConstExp ']' } ( # | '=' InitVal) #表示空串
    private Word ident;
    private ArrayList<ConstExp> constExpList;
    private InitVal initVal;
    public VarDef(Word ident, ArrayList<ConstExp> constExpList, InitVal initVal){
        this.ident = ident;
        this.constExpList = constExpList;
        this.initVal = initVal;
    }
    public VarDef(Word ident, ArrayList<ConstExp> constExpList){
        this.ident = ident;
        this.constExpList = constExpList;
        this.initVal = null;
    }

    public ArrayList<ConstExp> getConstExpList() {
        return constExpList;
    }

    public Word getIdent() {
        return ident;
    }

    public InitVal getInitVal() {
        return initVal;
    }

    public void printSyntax(){
        System.out.println("<VarDef>");
    }
}
