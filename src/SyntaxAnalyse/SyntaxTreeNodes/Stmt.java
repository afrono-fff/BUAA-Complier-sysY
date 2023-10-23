package SyntaxAnalyse.SyntaxTreeNodes;

import LexicalAnalyse.Word;

import java.text.Format;
import java.util.ArrayList;

public class Stmt {
    /* Stmt → LVal '=' Exp ';'
            | [Exp] ';'
            | Block
            | 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
            | 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
            | 'break' ';'
            | 'continue' ';'
            | 'return' [Exp] ';'
            | LVal '=' 'getint''('')'';'
            | 'printf''('FormatString{','Exp}')'';'
     */
    private LVal lVal;
    private Exp exp;
    private Block block;
    private Cond cond;
    private Stmt stmt;
    private Stmt elseStmt;
    private ForStmt forStmt1;
    private ForStmt forStmt2;
    private Word forTerminal;
    private Word formatString;
    private ArrayList<Exp> expList;

    //错误处理g使用:
    private boolean isReturn;
    public void setReturn(){this.isReturn = true;}
    public boolean isReturnStmt(){return isReturn;}
    public Stmt(LVal lVal, Exp exp){
        // LVal '=' Exp ';'
        this.lVal = lVal;
        this.exp = exp;
    }
    public Stmt(Exp exp){
        // [Exp] ';'
        // 'return' [Exp] ';'
        this.exp = exp;
    }
    public Stmt(Block block){
        // Block
        this.block = block;
    }
    public Stmt(Cond cond, Stmt stmt, Stmt elseStmt){
        // 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
        this.cond = cond;
        this.stmt = stmt;
        this.elseStmt = elseStmt;
    }
    public Stmt(Cond cond, Stmt stmt){
        // 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
        this.cond = cond;
        this.stmt = stmt;
    }
    public Stmt(ForStmt forStmt1, Cond cond, ForStmt forStmt2, Stmt stmt){
        // 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
        this.forStmt1 = forStmt1;
        this.cond = cond;
        this.forStmt2 = forStmt2;
        this.stmt = stmt;
    }
    public Stmt(Word forTerminal){
        // 'break' ';' | 'continue' ';'
        this.forTerminal = forTerminal;
    }
    public Stmt(LVal lVal){
        // LVal '=' 'getint''('')'';'
        this.lVal = lVal;
    }
    public Stmt(Word formatString, ArrayList<Exp> expList){
        // 'printf''('FormatString{','Exp}')'';'
        this.formatString = formatString;
        this.expList = expList;
    }
    public Stmt(){
        super();
    }
    public void printSyntax(){
        System.out.println("<Stmt>");
    }
}
