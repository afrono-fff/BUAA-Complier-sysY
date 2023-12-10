package LlvmGenerate.Instructions.Memories;

import LlvmGenerate.BasicBlock;
import LlvmGenerate.Instructions.Instruction;

public class LoadIns extends Instruction {
    private String name;
    private String valType;
    private String addrType;
    private String addrVal;
    public LoadIns(BasicBlock basicBlock,String name, String valType, String addrType, String addrVal) {
        super(basicBlock);
        this.name = name;
        this.valType = valType;
        this.addrType = addrType;
        this.addrVal = addrVal;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return this.name + " = load " + this.valType + ", " + this.addrType + " " + this.addrVal;
    }
}
