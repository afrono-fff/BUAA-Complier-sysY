package SyntaxAnalyse.SyntaxTreeNodes;

import LexicalAnalyse.Number;
import SymbolTable.FuncSymbol;
import SymbolTable.Symbol;
import SymbolTable.VarSymbol;
import SyntaxAnalyse.SyntaxAnalyzer;

public class PrimaryExp {
    // PrimaryExp → '(' Exp ')' | LVal | Number
    private Exp exp;
    private LVal lVal;
    private Number number;
    public PrimaryExp(Exp exp){ // 选Exp
        this.exp = exp;
        this.lVal = null;
        this.number = null;
    }
    public PrimaryExp(LVal lVal){ // 选LVal
        this.lVal = lVal;
        this.exp = null;
        this.number = null;
    }
    public PrimaryExp(Number number){ // 选Number
        this.number = number;
        this.exp = null;
        this.lVal = null;
    }
    public void printSyntax(){
        System.out.println("<PrimaryExp>");
    }
    public int getDimension(){
        if(exp != null){
            return exp.getDimension();
        }else if(lVal != null){
            // 变量
            return lVal.getDimension();
        }else{
            //Number
            return 0;
        }
    }
}
