package SyntaxAnalyse.SyntaxTreeNodes;

import LexicalAnalyse.Word;

public class UnaryOp {
    // UnaryOp → '+' | '−' | '!'
    private Word terminal;
    public UnaryOp(Word terminal){
        this.terminal = terminal;
    }
    public void printSyntax(){
        System.out.println("<UnaryOp>");
    }
}
