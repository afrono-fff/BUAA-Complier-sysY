package LlvmGenerate.Instructions.Calculations;

import LlvmGenerate.BasicBlock;
import LlvmGenerate.Instructions.Instruction;

public class AndIns extends Instruction {
    private String regName;
    private String valType;
    private String left;
    private String right;
    public AndIns(BasicBlock basicBlock, String regName, String valType, String left, String right) {
        super(basicBlock);
        this.regName = regName;
        this.valType = valType;
        this.left = left;
        this.right = right;
    }

    public String getRegName() {
        return regName;
    }

    @Override
    public String toString() {
        return regName + " = and " + valType + " " + left + ", " + right;
    }
}
