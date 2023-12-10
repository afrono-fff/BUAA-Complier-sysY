package LlvmGenerate.Definitions;

import LlvmGenerate.MiddleVal;

import java.util.ArrayList;

public class GlobalDecl {
    private String name;
    private String valueType; // 数值类型 i32 i32* i32**
    private MiddleVal v_r; // 数值或寄存器
    private int innerLen; // 一维数组长度（右边的中括号值）
    private ArrayList<MiddleVal> innerValList; // 一维数组
    private ArrayList<ArrayList<MiddleVal>> outerValList; // 二维数组
    private int outerLen; // 一维数组的数组长度（左边的中括号值）
    private boolean isConst;
    public GlobalDecl(String name, String valueType, MiddleVal v_r, boolean isConst){
        this.name = name;
        this.valueType = valueType;
        this.v_r = v_r;
        this.isConst = isConst;
    }
    public GlobalDecl(String name, String valueType, ArrayList<MiddleVal> innerValList, boolean isConst){
        this.name = name;
        this.valueType = valueType;
        this.innerValList = innerValList;
        this.isConst = isConst;
        this.innerLen = innerValList.size();
    }
    public GlobalDecl(String name, ArrayList<ArrayList<MiddleVal>> outerValList, boolean isConst){
        this.name = name;
        this.valueType = "i32**";
        this.outerValList = outerValList;
        this.isConst = isConst;
        this.outerLen = outerValList.size();
        this.innerLen = outerValList.get(0).size();
    }
    public boolean isAllZero(ArrayList<MiddleVal> innerValList){
        for(MiddleVal v_r: innerValList){
            if(!v_r.toString().equals("0")){
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        String v_c = isConst?"constant":"global";
        if(valueType.equals("i32")){ // 普通变量
            return "@" + this.name + " = " + v_c + " " + this.valueType + " " + this.v_r.getIntCon();
        }else if(valueType.equals("i32*")){ // 一维数组
            StringBuilder str = new StringBuilder();
            str.append("@").append(this.name).append(" = ").append(v_c).append(" [").append(this.innerLen).append(" x i32] ");
            if(isAllZero(innerValList)){
                str.append("zeroinitializer");
            }else{
                str.append("[");
                for(MiddleVal v_r: innerValList){
                    str.append(v_r.getValType()).append(" ").append(v_r).append(", ");
                }
                str.delete(str.length()-2, str.length());
                str.append("]");
            }
            return str.toString();
        }else if(valueType.equals("i32**")){ // 二维数组
            StringBuilder str = new StringBuilder();
            str.append("@").append(this.name).append(" = ").append(v_c).append(" [").append(this.outerLen).append(" x [").append(innerLen).append(" x i32]] ");
            boolean twoDAllZero = true;
            for(ArrayList<MiddleVal> inner: outerValList){
                if(!isAllZero(inner)){
                    twoDAllZero = false;
                }
            }
            if(twoDAllZero){
                str.append("zeroinitializer");
            }else{
                str.append("[");
                for(ArrayList<MiddleVal> inner: outerValList){
                    str.append("[").append(this.innerLen).append(" x i32] ");
                    if(isAllZero(inner)){
                        str.append("zeroinitializer");
                    }else{
                        str.append("[");
                        for(MiddleVal v_r: inner){
                            str.append(v_r.getValType()).append(" ").append(v_r).append(", ");
                        }
                        str.delete(str.length()-2, str.length());
                        str.append("]");
                    }
                    str.append(", ");
                }
                str.delete(str.length()-2, str.length());
                str.append("]");
            }
            return str.toString();
        }
        return "TODO!";
    }
}
