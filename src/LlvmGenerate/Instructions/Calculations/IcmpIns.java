package LlvmGenerate.Instructions.Calculations;

import LlvmGenerate.BasicBlock;
import LlvmGenerate.Instructions.Instruction;

public class IcmpIns extends Instruction {
    private String regName;
    private String cmpMethod; // eq ne sgt sge slt sle
    private String valType;
    private String left;
    private String right;
    public IcmpIns(BasicBlock basicBlock, String regName, String cmpMethod, String valType, String left, String right) {
        super(basicBlock);
        this.regName = regName;
        this.cmpMethod = cmpMethod;
        this.valType = valType;
        this.left = left;
        this.right = right;
    }

    public String getRegName() {
        return regName;
    }

    @Override
    public String toString() {
        return this.regName + " = icmp " + this.cmpMethod + " " + this.valType + " " + this.left + ", " + this.right;
    }
}
