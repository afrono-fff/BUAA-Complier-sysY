package SyntaxAnalyse.SyntaxTreeNodes;

public class BlockItem {
    //  BlockItem → Decl | Stmt
    private Decl decl;
    private Stmt stmt;
    public BlockItem(Decl decl){
        this.decl = decl;
        this.stmt = null;
    }
    public BlockItem(Stmt stmt){
        this.stmt = stmt;
        this.decl = null;
    }
    public Stmt getStmt(){
        return stmt;
    }
    public Decl getDecl(){
        return decl;
    }
}
