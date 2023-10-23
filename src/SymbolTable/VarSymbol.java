package SymbolTable;

import LexicalAnalyse.Word;

import java.util.ArrayList;

public class VarSymbol extends Symbol{
    private int dimension; // 维数 0普通变量 1一维数组 2二维数组
    private ArrayList<Integer> length; // 每个维度的长度 dimension=0时为空
    private boolean isConst; // 是否为const
    public VarSymbol(String token, int layer, int dimension, ArrayList<Integer> length, boolean isConst) {
        super(token, layer);
        this.dimension = dimension;
        this.length = length;
        this.isConst = isConst;
    }
    public boolean varIsConst(){
        return isConst;
    }
    public int getDimension(){
        return dimension;
    }
}
