package SyntaxAnalyse.SyntaxTreeNodes;

import LexicalAnalyse.Word;

public class FuncDef {
    // FuncType Ident '(' [FuncFParams] ')' Block
    private FuncType funcType;
    private Word ident;
    private FuncFParams funcFParams;
    private Block block;
    public FuncDef(FuncType funcType, Word ident, FuncFParams funcFParams, Block block){ // 有FuncFParams
        this.funcType = funcType;
        this.ident = ident;
        this.funcFParams = funcFParams;
        this.block = block;
    }
    public FuncDef(FuncType funcType, Word ident, Block block){ // 无FuncFParams
        this.funcType = funcType;
        this.ident = ident;
        this.block = block;
        this.funcFParams = null;
    }
    public void printSyntax(){
        System.out.println("<FuncDef>");
    }
}
