package LlvmGenerate.Instructions.Jump;

import LlvmGenerate.BasicBlock;
import LlvmGenerate.Instructions.Instruction;
import LlvmGenerate.MiddleVal;

public class BrIns extends Instruction {
    private boolean withRestriction;
    private MiddleVal icmpResult;
    private String ifTrueLabel;
    private String ifFalseLabel;
    private String destLabel;
    public BrIns(BasicBlock basicBlock, boolean withRestriction, MiddleVal icmpResult, String ifTrueLabel, String ifFalseLabel, String destLabel) {
        super(basicBlock);
        this.withRestriction = withRestriction;
        this.icmpResult = icmpResult;
        this.ifTrueLabel = ifTrueLabel;
        this.ifFalseLabel = ifFalseLabel;
        this.destLabel = destLabel;
    }

    @Override
    public String toString() {
        if(withRestriction){
            return "br i1 " + icmpResult + ", label %" + ifTrueLabel + ", label %" + ifFalseLabel;
        }else{
            return "br " + "label %" + destLabel;
        }
    }
}
