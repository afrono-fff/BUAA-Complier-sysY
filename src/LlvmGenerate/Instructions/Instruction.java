package LlvmGenerate.Instructions;

import LlvmGenerate.BasicBlock;
import LlvmGenerate.Instructions.Jump.BrIns;
import LlvmGenerate.Instructions.Jump.RetIns;

public class Instruction {
    BasicBlock basicBlock;
    public Instruction(BasicBlock basicBlock){
        this.basicBlock = basicBlock;
    }
    public boolean isTerminalIns(){
        if(this instanceof BrIns || this instanceof RetIns){
            return true;
        }
        return false;
    }
}
