package LlvmGenerate;

import LlvmGenerate.Instructions.Instruction;

public class MiddleVal {
    private String type; // intConst  register
    private int intCon;
    private String register;
    private String valType; // i32 i1 ...
    private Instruction instruction;
    public MiddleVal(){
        this.valType = "i32";
    }
    public MiddleVal(String type, int intCon, String register, Instruction instruction){
        this.type = type;
        this.intCon = intCon;
        this.register = register;
        this.instruction = instruction;
        this.valType = "i32"; // 默认为i32,按需使用set方法更改
    }

    public int getIntCon() {
        return intCon;
    }

    public Instruction getInstruction() {
        return instruction;
    }

    public String getRegister() {
        return register;
    }

    public String getType() {
        return type;
    }

    public void setIntCon(int intCon) {
        this.type = "intConst";
        this.intCon = intCon;
    }

    public void setRegister(String register) {
        this.type = "register";
        this.register = register;
    }

    public void setInstruction(Instruction instruction) {
        this.type = "instruction";
        this.instruction = instruction;
    }

    public void setValType(String valType) {
        this.valType = valType;
    }

    public String getValType() {
        return valType;
    }

    public void setType(String type) {
        this.type = type;
    }

    public MiddleVal copy(){
        MiddleVal middleVal = new MiddleVal(this.type, this.intCon, this.register, this.instruction);
        middleVal.setValType(this.valType);
        return middleVal;
    }
    @Override
    public String toString() {
        if(type.equals("intConst")){
            return intCon+"";
        }else if(type.equals("register")){
            return register;
        }else if(type.equals("instruction")){
            return instruction.toString();
        }
        return null;
    }
}
