package SyntaxAnalyse.SyntaxTreeNodes;

import LexicalAnalyse.Word;
import SymbolTable.FuncSymbol;
import SymbolTable.Symbol;
import SyntaxAnalyse.SyntaxAnalyzer;

public class UnaryExp {
    // UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
    private PrimaryExp primaryExp;
    Word ident;
    private FuncRParams funcRParams;
    private UnaryOp unaryOp;
    private UnaryExp unaryExp;
    public UnaryExp(PrimaryExp primaryExp){ // 选择左边
        this.primaryExp = primaryExp;
        this.ident = null;
        this.funcRParams = null;
        this.unaryOp = null;
        this.unaryExp = null;
    }
    public UnaryExp(Word ident){ // 选择中间且不选FuncRParams
        this.ident = ident;
        this.primaryExp = null;
        this.funcRParams = null;
        this.unaryOp = null;
        this.unaryExp = null;
    }
    public UnaryExp(Word ident, FuncRParams funcRParams){ // 选择中间且选择FuncRParams
        this.ident = ident;
        this.funcRParams = funcRParams;
        this.primaryExp = null;
        this.unaryOp = null;
        this.unaryExp = null;
    }
    public UnaryExp(UnaryOp unaryOp, UnaryExp unaryExp){ // 选择右边
        this.primaryExp = null;
        this.ident = null;
        this.funcRParams = null;
        this.unaryOp = unaryOp;
        this.unaryExp = unaryExp;
    }
    public void printSyntax(){
        System.out.println("<UnaryExp>");
    }
    public int getDimension(){
        if(primaryExp != null){
            //primaryExp
            return primaryExp.getDimension();
        }else if(ident != null){
            //函数
            Symbol s;
            if((s = SyntaxAnalyzer.searchSymbol(ident.getToken())) != null){
                if(s instanceof FuncSymbol){
                    FuncSymbol fs = (FuncSymbol) s;
                    if(fs.voidFunc()){
                        return -1;
                    }else {
                        return 0;
                    }
                }
            }
        }else{
            return unaryExp.getDimension(); // 递归
        }
        return -1;
    }
}
