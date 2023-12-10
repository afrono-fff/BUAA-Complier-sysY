package SyntaxAnalyse.SyntaxTreeNodes;

import java.util.concurrent.BlockingDeque;

public class MainFuncDef {
    private Block block;
    public MainFuncDef(Block block){
        this.block = block;
    }
    public void printSyntax(){
        System.out.println("<MainFuncDef>");
    }
    public Block getBlock(){
        return this.block;
    }
}
