package SyntaxAnalyse.SyntaxTreeNodes;

import LexicalAnalyse.Word;
import SymbolTable.Symbol;
import SymbolTable.VarSymbol;
import SyntaxAnalyse.SyntaxAnalyzer;

import java.util.ArrayList;

public class LVal {
    //  LVal → Ident {'[' Exp ']'}
    private Word ident;
    private ArrayList<Exp> expList;
    public LVal(Word ident, ArrayList<Exp> expList){
        this.ident = ident;
        this.expList = expList;
    }
    public void printSyntax(){
        System.out.println("<LVal>");
    }
    public Word getIdent(){
        return ident;
    }

    public ArrayList<Exp> getExpList() {
        return expList;
    }

    public int getDimension(){ // 例如声明a[][]=...,那么a[0]就是1维的
        VarSymbol vs;
        if((vs = (VarSymbol) SyntaxAnalyzer.searchSymbol(ident.getToken())) != null){
            return vs.getDimension() - expList.size(); // 变量（普通变量或数组变量）的dimension
        }else {
            // 实际是函数实参中使用了未声明的变量
            return -2;
        }
    }
}
