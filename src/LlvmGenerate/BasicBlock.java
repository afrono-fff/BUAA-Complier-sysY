package LlvmGenerate;

import LlvmGenerate.Instructions.Instruction;

import java.util.ArrayList;

public class BasicBlock {
    private String label;
    private ArrayList<Instruction> instructionList;
    public BasicBlock(String label){
        this.label = label;
        this.instructionList = new ArrayList<>();
    }
    public void addIns(Instruction instruction){
        this.instructionList.add(instruction);
    }

    public String getLabel() {
        return label;
    }

    public ArrayList<Instruction> getInstructionList() {
        return instructionList;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        if(!label.equals("start")){
            str.append(this.label).append(":\n");
        }
        for(Instruction instruction: instructionList){
            str.append("\t").append(instruction.toString()).append("\n");
        }
        str.append("\n");
        return str.toString();
    }
}
