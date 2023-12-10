package LlvmGenerate.Instructions.Calculations;

import LlvmGenerate.BasicBlock;
import LlvmGenerate.Instructions.Instruction;

public class SremIns extends Instruction {
    private String regName;
    private String type;
    private String left;
    private String right;
    public SremIns(BasicBlock basicBlock, String regName, String type, String left, String right) {
        super(basicBlock);
        this.regName = regName;
        this.type = type;
        this.left = left;
        this.right = right;
    }

    public String getRegName() {
        return regName;
    }

    @Override
    public String toString() {
        return regName + " = srem " + type + " " + left + ", " + right;
    }
}
