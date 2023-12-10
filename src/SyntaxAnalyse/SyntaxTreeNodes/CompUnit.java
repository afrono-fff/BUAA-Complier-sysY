package SyntaxAnalyse.SyntaxTreeNodes;

import com.sun.tools.javac.Main;

import java.util.ArrayList;

public class CompUnit{ // 语法树根节点：编译单元
    private ArrayList<Decl> declList; // {Decl}子节点
    private ArrayList<FuncDef> funcDefList; // {FuncDef}子节点
    private MainFuncDef mainFuncDef; // MainFuncDef子节点
    public CompUnit(ArrayList<Decl> declList, ArrayList<FuncDef> funcDefList, MainFuncDef mainFuncDef){
        this.declList = declList;
        this.funcDefList = funcDefList;
        this.mainFuncDef = mainFuncDef;
    }
    public void printSyntax(){
        System.out.println("<CompUnit>");
    }
    public ArrayList<Decl> getDeclList(){
        return this.declList;
    }
    public ArrayList<FuncDef> getFuncDefList(){
        return this.funcDefList;
    }
    public MainFuncDef getMainFuncDef(){
        return this.mainFuncDef;
    }
}
