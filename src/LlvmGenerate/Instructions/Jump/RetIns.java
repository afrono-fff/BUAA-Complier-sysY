package LlvmGenerate.Instructions.Jump;

import LlvmGenerate.BasicBlock;
import LlvmGenerate.Instructions.Instruction;
import LlvmGenerate.MiddleVal;

public class RetIns extends Instruction {
    private boolean isVoid;
    private String retType;
    private MiddleVal retVal;
    public RetIns(BasicBlock basicBlock, String retType, MiddleVal retVal){
        super(basicBlock);
        this.isVoid = false;
        this.retType = retType;
        this.retVal = retVal;
    }
    public RetIns(BasicBlock basicBlock) {
        super(basicBlock);
        this.isVoid = true;
    }

    @Override
    public String toString() {
        if(isVoid){
            return "ret void";
        }else {
            return "ret " +retType + " " + retVal.toString();
        }
    }
}
