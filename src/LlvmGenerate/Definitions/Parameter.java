package LlvmGenerate.Definitions;

import LlvmGenerate.MiddleVal;

public class Parameter {
    private String type;
    private MiddleVal value;
    public Parameter(String type, MiddleVal value){
        this.type = type;
        this.value = value;
    }

    @Override
    public String toString() {
        return type+ " " + value.toString();
    }
}
