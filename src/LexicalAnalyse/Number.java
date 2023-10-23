package LexicalAnalyse;

public class Number extends Word{
    private int number;
    public Number(LexicalType type, String token, int line){
        super(type, token, line);
        this.number = Integer.parseInt(token);
    }
    public int getNumber(){
        return number;
    }
}
