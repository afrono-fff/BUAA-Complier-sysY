package LlvmGenerate.Instructions.Memories;

import LlvmGenerate.BasicBlock;
import LlvmGenerate.Instructions.Instruction;

public class AllocaIns extends Instruction {
    private String name;
    private String type;
    public AllocaIns(BasicBlock basicBlock, String name, String type) {
        super(basicBlock);
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return this.name + " = alloca " + this.type;
    }
}
