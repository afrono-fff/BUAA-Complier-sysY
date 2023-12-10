package SyntaxAnalyse.SyntaxTreeNodes;

import LexicalAnalyse.Word;

import java.util.ArrayList;

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
        this.funcFParams = new FuncFParams(new ArrayList<>());
    }

    public Word getIdent() {
        return ident;
    }

    public Block getBlock() {
        return block;
    }

    public FuncFParams getFuncFParams() {
        return funcFParams;
    }

    public FuncType getFuncType() {
        return funcType;
    }

    public void printSyntax(){
        System.out.println("<FuncDef>");
    }
}
