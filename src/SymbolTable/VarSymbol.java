package SymbolTable;

import LexicalAnalyse.Word;
import LlvmGenerate.MiddleVal;

import java.awt.image.AreaAveragingScaleFilter;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class VarSymbol extends Symbol{
    private int dimension; // 维数 0普通变量 1一维数组 2二维数组
    private ArrayList<Integer> length; // 每个维度的长度 dimension=0时为空
    private boolean isConst; // 是否为const
    private MiddleVal value; // 值或寄存器
    private ArrayList<ArrayList<MiddleVal>> arrayValue; // 数组数据值或寄存器
    private String storeReg; // 存储寄存器，非空表示局部变量的访问需要通过load指令
    private boolean isPointer; // 表示此符号代表函数形参中的数组变量，在使用时需要先将指针类型load取数组地址
    public VarSymbol(String token, int layer, int dimension, ArrayList<Integer> length, boolean isConst) {
        super(token, layer);
        this.dimension = dimension;
        this.length = length;
        this.isConst = isConst;
    }
    // 在错误处理阶段未加入value属性，为了防止造成前置问题，在代码生成阶段新创建一个构造方法
    public VarSymbol(String token, int layer, int dimension, ArrayList<Integer> length, boolean isConst, MiddleVal value){
        super(token, layer);
        this.dimension = dimension;
        this.length = length;
        this.isConst = isConst;
        this.value = value;
        this.storeReg = null;
    }
    public VarSymbol(String token, int layer, int dimension, ArrayList<Integer> length, boolean isConst, ArrayList<ArrayList<MiddleVal>> arrayValue){
        super(token, layer);
        this.dimension = dimension;
        this.length = length;
        this.isConst = isConst;
        this.arrayValue = arrayValue;
        this.storeReg = null;
    }
    public boolean varIsConst(){
        return isConst;
    }
    public int getDimension(){
        return dimension;
    }

    public MiddleVal getValue() {
        return value;
    }

    public void setStoreReg(String storeReg) {
        this.storeReg = storeReg;
    }

    public String getStoreReg() {
        return storeReg;
    }

    public void setValue(MiddleVal value) {
        this.value = value;
    }
    public ArrayList<ArrayList<MiddleVal>> getArrayValue(){
        return arrayValue;
    }

    public void setPointer(boolean isPointer) {
        this.isPointer = isPointer;
    }

    public boolean isPointer() {
        return isPointer;
    }

    public void setLength(ArrayList<Integer> length) {
        this.length = length;
    }

    public ArrayList<Integer> getLength() {
        return length;
    }
}
