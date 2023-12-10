package LlvmGenerate.Instructions.Memories;

import LlvmGenerate.BasicBlock;
import LlvmGenerate.Instructions.Instruction;

public class ZextToIns extends Instruction {
    private String regName;
    private String fromType;
    private String value;
    private String toType;
    public ZextToIns(BasicBlock basicBlock, String regName, String fromType, String value, String toType) {
        super(basicBlock);
        this.regName = regName;
        this.fromType = fromType;
        this.toType = toType;
        this.value = value;
    }

    public String getRegName() {
        return regName;
    }

    @Override
    public String toString() {
        return regName + " = zext " + fromType + " " + value + " to " + toType;
    }
}
