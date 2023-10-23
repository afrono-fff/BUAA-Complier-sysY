package LexicalAnalyse;

public class Word {
    private LexicalType type; //单词类型码
    private String token; //单词值
    private int line; //所在行号

    public Word(LexicalType type,String token,int line){
        this.type = type;
        this.token = token;
        this.line = line;
    }

    public LexicalType getType(){
        return this.type;
    }
    public  String getToken(){
        return this.token;
    }

    public int getLine() {
        return this.line;
    }

    @Override
    public String toString() {
        if(this.type != LexicalType.NONE){
            return this.type + " " + this.token/* + " " + this.line*/+"\n";
        }
        return "";
    }
}
