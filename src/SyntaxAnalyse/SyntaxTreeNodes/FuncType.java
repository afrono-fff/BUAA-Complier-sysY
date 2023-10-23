package SyntaxAnalyse.SyntaxTreeNodes;

import LexicalAnalyse.LexicalType;
import LexicalAnalyse.Word;

public class FuncType{
    // FuncType → 'void' | 'int'
    private Word terminal;
    public FuncType(Word terminal){
        this.terminal = terminal;
    }
    public void printSyntax(){
        System.out.println("<FuncType>");
    }
    public boolean isInt(){
        return terminal.getType() == LexicalType.INTTK;
    }
}
