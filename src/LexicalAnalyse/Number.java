package LexicalAnalyse;

public class Number extends Word{
    private int number;
    public Number(LexicalType type, String token, int line, int number){
        super(type, token, line);
        this.number = number;
    }
}
