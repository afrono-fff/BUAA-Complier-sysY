package LlvmGenerate;

import LlvmGenerate.Definitions.Function;
import LlvmGenerate.Definitions.GlobalDecl;

import java.util.ArrayList;

public class LlvmTree {
    // llvm_ir文件就是全局变量声明和函数体的集合
    private ArrayList<GlobalDecl> globalDeclList;
    private ArrayList<Function> functionList;
    public LlvmTree(){
        this.functionList = new ArrayList<>();
        this.globalDeclList = new ArrayList<>();
    }
    public void addFunction(Function function){
        this.functionList.add(function);
    }
    public void addGlobalDecl(GlobalDecl globalDecl){
        this.globalDeclList.add(globalDecl);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("declare i32 @getint()\n").append("declare void @putint(i32)\n").append("declare void @putch(i32)\n").append("declare void @putstr(i8*)\n");
        for (GlobalDecl globalDecl : globalDeclList) {
            str.append(globalDecl.toString());
            str.append("\n");
        }
        str.append("\n");
        for (Function function : functionList) {
            str.append(function.toString());
            str.append("\n");
        }
        return str.toString();
    }
}
