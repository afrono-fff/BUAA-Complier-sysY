package SyntaxAnalyse.SyntaxTreeNodes;

import java.util.ArrayList;

public class FuncFParams {
    //  FuncFParams → FuncFParam { ',' FuncFParam }
    ArrayList<FuncFParam> funcFParamList;
    public FuncFParams(ArrayList<FuncFParam> funcFParamList){
        this.funcFParamList = funcFParamList;
    }
    public void printSyntax(){
        System.out.println("<FuncFParams>");
    }
    public int getParamNum(){
        return funcFParamList.size();
    }
    public ArrayList<FuncFParam> getFuncFParamList(){
        return funcFParamList;
    }
    public ArrayList<Integer> getDimensions(){
        ArrayList<Integer> dimensions = new ArrayList<>();
        for(FuncFParam ffp:funcFParamList){
            dimensions.add(ffp.getDimension());
        }
        return dimensions;
    }
    public FuncFParams copy(){
        return new FuncFParams(this.funcFParamList);
    }
}
