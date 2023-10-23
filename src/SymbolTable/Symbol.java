package SymbolTable;

import LexicalAnalyse.Word;

public class Symbol {
    // Symbol类 包含两个子类：ValSymbol和FuncSymbol
    private String token; // 符号：标识符
    private int layer; // 符号所在的层次
    public Symbol(String token, int layer){
        this.token = token;
        this.layer = layer;
    }
    public String getToken(){
        return token;
    }
}
