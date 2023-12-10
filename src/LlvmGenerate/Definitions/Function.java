package LlvmGenerate.Definitions;
import LlvmGenerate.BasicBlock;
import java.util.ArrayList;

public class Function {
    private String name;
    private String retType;
    private ArrayList<Parameter> parameterList;
    private ArrayList<BasicBlock> basicBlockList; // 函数体看成是若干个基本块的集合
    public Function(String name, String retType, ArrayList<Parameter> parameterList, ArrayList<BasicBlock> basicBlockList){
        this.name = name;
        this.retType = retType;
        this.parameterList = new ArrayList<>(parameterList);
        this.basicBlockList = new ArrayList<>(basicBlockList);
    }

    public ArrayList<BasicBlock> getBasicBlockList() {
        return basicBlockList;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("define ").append(retType).append(" ").append(name).append("(");
        if(!parameterList.isEmpty()){
            for(Parameter parameter:parameterList){
                str.append(parameter.toString());
                str.append(", ");
            }
            str.delete(str.length()-2, str.length()); // 删去末尾的','
        }
        str.append("){");
        str.append("\n");
        for(BasicBlock basicBlock:basicBlockList){
            str.append(basicBlock.toString());
        }
        str.delete(str.length()-1, str.length()); // 删去最后一个basicBlock的换行
        str.append("}\n");
        return str.toString();
    }
}
