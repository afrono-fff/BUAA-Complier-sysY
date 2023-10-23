package SyntaxAnalyse.SyntaxTreeNodes;

import LexicalAnalyse.Word;

import java.util.ArrayList;

public class FuncFParam {
    //  FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }]
    private BType bType;
    private Word ident;
    private int dimension;
    private ArrayList<ConstExp> constExpList;
    public FuncFParam(BType bType, Word ident, ArrayList<ConstExp> constExpList){
        this.bType = bType;
        this.ident = ident;
        this.constExpList = constExpList;
    }
    public FuncFParam(BType bType, Word ident){ // 非数组参数
        this.bType = bType;
        this.ident = ident;
        this.constExpList = null;
    }
    public void printSyntax(){
        System.out.println("<FuncFParam>");
    }
    public Word getIdent(){
        return ident;
    }
    public void setDimension(int dimension){
        this.dimension = dimension;
    }
    public int getDimension(){
        return dimension;
    }
}
