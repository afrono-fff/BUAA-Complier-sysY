package SymbolTable;

import LexicalAnalyse.Word;
import SyntaxAnalyse.SyntaxTreeNodes.FuncFParams;
import SyntaxAnalyse.SyntaxTreeNodes.FuncType;

import java.util.ArrayList;

public class FuncSymbol extends Symbol{
    private boolean isVoidFunc; // 无返回函数
    private int paramNum; // 参数个数
    private ArrayList<Integer> dimensions; // 参数类型（paraNum = dimensions.length）
    public FuncSymbol(String token, int layer, boolean isVoidFunc, int paramNum, ArrayList<Integer> dimensions) {
        super(token, layer);
        this.isVoidFunc = isVoidFunc;
        this.paramNum = paramNum;
        this.dimensions = dimensions;
    }
    public int getParamNum(){
        return paramNum;
    }
    public ArrayList<Integer> getDimensions(){
        return dimensions;
    }
    public boolean voidFunc(){
        return isVoidFunc;
    }
}
