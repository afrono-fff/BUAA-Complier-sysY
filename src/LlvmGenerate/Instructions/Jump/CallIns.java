package LlvmGenerate.Instructions.Jump;

import LlvmGenerate.BasicBlock;
import LlvmGenerate.Definitions.Parameter;
import LlvmGenerate.Instructions.Instruction;
import java.util.ArrayList;

public class CallIns extends Instruction {
    private boolean isVoid;
    private String funName;
    private String regName;
    private String retType;
    private ArrayList<Parameter> parameterList;
    public CallIns(BasicBlock basicBlock, boolean isVoid, String funName, String regName, String retType, ArrayList<Parameter>parameterList) {
        super(basicBlock);
        this.isVoid = isVoid;
        this.funName = funName;
        this.regName = regName;
        this.retType = retType;
        this.parameterList = parameterList;
    }

    public String getRegName() {
        return regName;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        String result = "";
        if(!isVoid){
            result = this.regName + " = ";
        }
        str.append(result).append("call ").append(this.retType).append(" ").append(this.funName).append("(");
        for(int i = 0; i < parameterList.size(); i ++ ){
            str.append(parameterList.get(i));
            if(i != parameterList.size() - 1){
                str.append(", ");
            }
        }
        str.append(")");
        return str.toString();
    }
}
