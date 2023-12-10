package LlvmGenerate.Instructions.Memories;

import LlvmGenerate.BasicBlock;
import LlvmGenerate.Instructions.Instruction;
import LlvmGenerate.MiddleVal;

public class GetelementptrIns extends Instruction {
    private String name;
    private String type; // 数组类型 [outerLen x [innerLen x i32]]
    String addressReg;
    MiddleVal offset0;
    MiddleVal offset1;
    MiddleVal offset2;
    public GetelementptrIns(String regName, BasicBlock basicBlock, String type, String addressReg, MiddleVal offset0, MiddleVal offset1, MiddleVal offset2) {
        super(basicBlock);
        this.name = regName;
        this.offset0 = offset0;
        this.offset1 = offset1;
        this.offset2 = offset2;
        this.addressReg = addressReg;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        if(offset1 == null) {
            return name + " = getelementptr " + type + ", " + type + "* " + addressReg + ", " + offset0.getValType() + " " + offset0;
        }else if(offset2 == null){
            return name + " = getelementptr " + type + ", " + type + "* " + addressReg + ", " + offset0.getValType() + " " + offset0 + ", " + offset1.getValType() + " " + offset1;
        }else{
            return name + " = getelementptr " + type + ", " + type + "* " + addressReg + ", " + offset0.getValType() + " " + offset0 + ", " + offset1.getValType() + " " + offset1 + ", " + offset2.getValType() + " " + offset2;
        }
    }
}
