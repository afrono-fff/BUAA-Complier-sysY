package SymbolTable;

import LexicalAnalyse.Word;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class SymbolTable {
    private int layer; // 层级
    private HashMap<String,Symbol> table; // 符号表
    private SymbolTable parent; // 父节点 最外层为null
    private ArrayList<SymbolTable> children; // 子节点
    public SymbolTable(SymbolTable parent){
        this.layer = parent.getLayer() + 1;
        this.table = new HashMap<>();
        this.parent = parent;
        this.children = new ArrayList<>();
    }
    public SymbolTable(){ // 最外层符号表(全局符号所在表)
        this.layer = 0;
        this.table = new HashMap<>();
        this.parent = null;
        this.children = new ArrayList<>();
    }
    public int getLayer(){
        return this.layer;
    }
    public SymbolTable getParent(){
        return this.parent;
    }
    public HashMap<String,Symbol> getTable(){
        return table;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for(String key:table.keySet()){
            str.append(table.get(key)).append("\n");
        }
        return str.toString();
    }
}
