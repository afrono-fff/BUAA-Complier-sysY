package SyntaxAnalyse.SyntaxTreeNodes;

import LexicalAnalyse.Word;
import LlvmGenerate.MiddleVal;

import java.util.ArrayList;

public class FuncFParam {
    //  FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }]
    private BType bType;
    private Word ident;
    private int dimension; // 0:普通变量  1:一维数组  2:二维数组
    private MiddleVal value; // 寄存器或值
    private ArrayList<ConstExp> constExpList;
    public FuncFParam(BType bType, Word ident, ArrayList<ConstExp> constExpList){
        this.bType = bType;
        this.ident = ident;
        this.constExpList = constExpList;
        this.value = null;
    }
    public FuncFParam(BType bType, Word ident){ // 非数组参数
        this.bType = bType;
        this.ident = ident;
        this.constExpList = null;
        this.value = null;
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

    public MiddleVal getValue() {
        return value;
    }

    public void setValue(MiddleVal value) {
        this.value = value;
    }

    public ArrayList<ConstExp> getConstExpList() {
        return constExpList;
    }
}
