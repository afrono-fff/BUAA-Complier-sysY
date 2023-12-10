package LlvmGenerate.Instructions.Memories;

import LlvmGenerate.BasicBlock;
import LlvmGenerate.Instructions.Instruction;

public class StoreIns extends Instruction {
    String valType; // 值类型
    String valValue; // 值寄存器或常量值
    String addrType; // 地址类型
    String addrValue; // 地址值
    public StoreIns(BasicBlock basicBlock, String valType, String valValue, String addrType, String addrValue) {
        super(basicBlock);
        this.valType = valType;
        this.valValue = valValue;
        this.addrType = addrType;
        this.addrValue = addrValue;
    }

    @Override
    public String toString() {
        return "store " + this.valType + " " + this.valValue + ", " + this.addrType + " " + this.addrValue;
    }
}
