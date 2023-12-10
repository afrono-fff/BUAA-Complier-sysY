package SyntaxAnalyse.SyntaxTreeNodes;

import java.util.ArrayList;

public class FuncRParams {
    //  FuncRParams → Exp { ',' Exp }
    private ArrayList<Exp> expList;
    public FuncRParams(ArrayList<Exp> expList){
        this.expList = expList;
    }

    public ArrayList<Exp> getExpList() {
        return expList;
    }

    public void printSyntax(){
        System.out.println("<FuncRParams>");
    }
    public int paraNum(){
        return expList.size();
    }
    public ArrayList<Integer> getDimensions(){
        ArrayList<Integer> dimensions = new ArrayList<>();
        for(Exp exp:expList){
            dimensions.add(exp.getDimension());
        }
        return dimensions;
    }
}
