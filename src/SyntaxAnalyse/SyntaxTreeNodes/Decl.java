package SyntaxAnalyse.SyntaxTreeNodes;

public class Decl {
    private ConstDecl constDecl;
    private VarDecl varDecl;
    public Decl(ConstDecl constDecl){
        this.constDecl = constDecl;
        this.varDecl = null;
    }
    public Decl(VarDecl varDecl){
        this.varDecl = varDecl;
        this.constDecl = null;
    }

    public ConstDecl getConstDecl() {
        return constDecl;
    }

    public VarDecl getVarDecl() {
        return varDecl;
    }
    // Decl不输出语法成分
}
