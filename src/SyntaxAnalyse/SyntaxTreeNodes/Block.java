package SyntaxAnalyse.SyntaxTreeNodes;

import java.util.ArrayList;
import java.util.List;

public class Block {
    // Block → '{' { BlockItem } '}'
    private ArrayList<BlockItem> blockItemList;
    public Block(ArrayList<BlockItem> blockItemList){
        this.blockItemList = blockItemList;
    }
    public void printSyntax(){
        System.out.println("<Block>");
    }
    public boolean withReturn(){
        if(blockItemList.isEmpty()){
            return false;
        }
        BlockItem lastItem = blockItemList.get(blockItemList.size() - 1);
        Stmt stmt;
        if((stmt = lastItem.getStmt()) != null){
            // BlockItem是Stmt,可能为return语句
            return stmt.isReturnStmt();
        }
        return false;
    }
    public ArrayList<BlockItem> getBlockItemList(){
        return blockItemList;
    }
}
